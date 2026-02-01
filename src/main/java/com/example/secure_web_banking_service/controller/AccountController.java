package com.example.secure_web_banking_service.controller;

import com.example.secure_web_banking_service.dto.TransferRequest;
import com.example.secure_web_banking_service.model.AppUser;
import com.example.secure_web_banking_service.model.Transaction;
import com.example.secure_web_banking_service.repository.TransactionRepository;
import com.example.secure_web_banking_service.repository.UserRepository;
import com.example.secure_web_banking_service.security.AesService;
import com.example.secure_web_banking_service.security.JwtService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final JwtService jwtService;
    private final UserRepository repo;
    private final AesService aesService;
    private final TransactionRepository txRepo;

    public AccountController(JwtService jwtService,
                             UserRepository repo,
                             AesService aesService,
                             TransactionRepository txRepo) {
        this.jwtService = jwtService;
        this.repo = repo;
        this.aesService = aesService;
        this.txRepo = txRepo;
    }

    // ------------------ COOKIE JWT HELPERS ------------------

    // Estrae il token JWT dal cookie "jwt"
    private String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if ("jwt".equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

    // Ritorna l'email dal JWT (subject), oppure null se token mancante/non valido
    private String getEmailOrNull(HttpServletRequest request) {
        String token = getJwtFromCookie(request);
        if (token == null) return null;

        if (!jwtService.isTokenValid(token)) return null;

        // Nel nostro progetto il subject del JWT è l'email
        return jwtService.extractEmail(token);
    }

    // ------------------ BALANCE HELPERS (AES) ------------------

    // Legge il saldo decifrando AES e convertendo in BigDecimal
    private BigDecimal readBalance(AppUser u) {
        String plain = aesService.decrypt(u.getEncryptedBalance());   // es: "10.50"
        return new BigDecimal(plain).setScale(2, RoundingMode.HALF_UP);
    }

    // Scrive il saldo cifrando AES (normalizzato a 2 decimali)
    private void writeBalance(AppUser u, BigDecimal balance) {
        BigDecimal normalized = balance.setScale(2, RoundingMode.HALF_UP);
        u.setEncryptedBalance(aesService.encrypt(normalized.toPlainString()));
    }

    // ------------------ /me ------------------
    // Restituisce dati utili per la home: nome+cognome (decifrati) e iban
    @GetMapping("/me")
    public Map<String, String> me(HttpServletRequest request) {
        String email = getEmailOrNull(request);
        if (email == null) return Map.of("error", "UNAUTHORIZED");

        AppUser u = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String firstName = aesService.decrypt(u.getEncryptedFirstName());
        String lastName = aesService.decrypt(u.getEncryptedLastName());

        return Map.of(
                "email", u.getEmail(),
                "fullName", firstName + " " + lastName,
                "iban", u.getIban()
        );
    }

    // ------------------ /balance ------------------
    // Restituisce saldo decifrato
    @GetMapping("/balance")
    public Map<String, String> getBalance(HttpServletRequest request) {
        String email = getEmailOrNull(request);
        if (email == null) return Map.of("error", "UNAUTHORIZED");

        AppUser u = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        BigDecimal bal = readBalance(u);

        return Map.of("balance", bal.toPlainString());
    }

    // ------------------ /transfer ------------------
    // Bonifico tramite IBAN: mittente = utente loggato, destinatario = utente con IBAN in DB
    // Aggiorna saldo mittente/destinatario (AES) e salva una Transaction
    @RateLimiter(name = "transferLimiter")
    @Transactional
    @PostMapping("/transfer")
    public Map<String, String> transfer(HttpServletRequest request,
                                        @Valid @RequestBody TransferRequest body) {

        String email = getEmailOrNull(request);
        if (email == null) return Map.of("error", "UNAUTHORIZED");

        // carica mittente tramite email
        AppUser from = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // IBAN destinatario normalizzato
        String toIban = body.toIban().trim().toUpperCase();

        // blocca bonifico verso il proprio IBAN
        if (from.getIban().equals(toIban)) {
            return Map.of("error", "SAME_USER");
        }

        // carica destinatario tramite IBAN
        AppUser to = repo.findByIban(toIban).orElse(null);
        if (to == null) {
            return Map.of("error", "DEST_NOT_FOUND");
        }

        // importo
        BigDecimal amount = body.amount().setScale(2, RoundingMode.HALF_UP);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Map.of("error", "INVALID_AMOUNT");
        }

        // leggi saldi
        BigDecimal fromBal = readBalance(from);
        BigDecimal toBal = readBalance(to);

        // controlla fondi
        if (fromBal.compareTo(amount) < 0) {
            return Map.of("error", "INSUFFICIENT_FUNDS");
        }

        // aggiorna saldi
        writeBalance(from, fromBal.subtract(amount));
        writeBalance(to, toBal.add(amount));

        repo.save(from);
        repo.save(to);

        Transaction tx = new Transaction();
        tx.setFromIban(from.getIban());
        tx.setToIban(to.getIban());
        tx.setAmount(amount);
        tx.setMemo(body.memo());
        tx.setCreatedAt(Instant.now());
        txRepo.save(tx);

        return Map.of(
                "status", "OK",
                "newBalance", readBalance(from).toPlainString()
        );
    }

    // ------------------ /transactions ------------------
    // Restituisce ultimi 20 movimenti dove l'utente compare come mittente o destinatario
    @GetMapping("/transactions")
    public Object transactions(HttpServletRequest request) {
        String email = getEmailOrNull(request);
        if (email == null) return Map.of("error", "UNAUTHORIZED");

        AppUser u = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        String myIban = u.getIban();

        List<Transaction> txs = txRepo.findTop20ByFromIbanOrToIbanOrderByCreatedAtDesc(myIban, myIban);

        // restituisce JSON "pulito" (stringhe)
        return txs.stream().map(tx -> Map.of(
                "id", String.valueOf(tx.getId()),
                "from", tx.getFromIban(),
                "to", tx.getToIban(),
                "amount", tx.getAmount().toPlainString(),
                "memo", tx.getMemo() == null ? "" : tx.getMemo(),
                "createdAt", tx.getCreatedAt().toString()
        )).toList();
    }
}
