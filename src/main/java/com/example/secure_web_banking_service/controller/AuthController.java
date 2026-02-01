package com.example.secure_web_banking_service.controller;

import com.example.secure_web_banking_service.dto.LoginRequest;
import com.example.secure_web_banking_service.dto.RegisterRequest;
import com.example.secure_web_banking_service.model.AppUser;
import com.example.secure_web_banking_service.repository.UserRepository;
import com.example.secure_web_banking_service.security.AesService;
import com.example.secure_web_banking_service.security.IbanService;
import com.example.secure_web_banking_service.security.JwtService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AesService aesService;
    private final IbanService ibanService;

    public AuthController(
            UserRepository repo,
            PasswordEncoder encoder,
            JwtService jwtService,
            AesService aesService,
            IbanService ibanService
    ) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.aesService = aesService;
        this.ibanService = ibanService;
    }

    // ------------------ REGISTER ------------------
    // Registra utente con: nome, cognome, email, telefono, password
    // - Email in chiaro (normalizzata) per login e vincolo unique
    // - Nome/Cognome/Telefono/ saldo cifrati AES (PII)
    // - Password hashata BCrypt
    // - IBAN fake valido generato e univoco
    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {

        // normalizza email
        String emailNorm = req.email().trim().toLowerCase();

        // email unica
        if (repo.existsByEmail(emailNorm)) {
            return "EMAIL_TAKEN";
        }

        AppUser u = new AppUser();

        // email in chiaro (serve per login)
        u.setEmail(emailNorm);

        // password hashata
        u.setPasswordHash(encoder.encode(req.password()));

        // Cifra nome, cognome e telefono
        u.setEncryptedFirstName(aesService.encrypt(req.firstName().trim()));
        u.setEncryptedLastName(aesService.encrypt(req.lastName().trim()));
        u.setEncryptedPhone(aesService.encrypt(req.phone().trim()));

        // genera IBAN fake univoco
        String iban;
        do {
            iban = ibanService.generateItalianIban();
        } while (repo.existsByIban(iban));
        u.setIban(iban);

        // saldo iniziale 50.00 (cifrato)
        u.setEncryptedBalance(aesService.encrypt("50.00"));

        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            // fallback (non dovrebbe succedere se frontend ok)
            deviceId = java.util.UUID.randomUUID().toString();
        }

        u.setDeviceId(deviceId);

        // salva utente
        repo.save(u);

        return "REGISTER_OK";
    }

    // ------------------ LOGIN ------------------
    // Login con email + password
    // Ritorna JWT (subject = email) che poi il frontend salva in cookie "jwt"
    @RateLimiter(name = "loginLimiter")
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {

        String emailNorm = req.email().trim().toLowerCase();

        String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            deviceId = java.util.UUID.randomUUID().toString();
        }

        AppUser u = repo.findByEmail(emailNorm)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("BAD_CREDENTIALS");
        }

        // salva device in DB (se vuoi “un device unico per account”)
        u.setDeviceId(deviceId);
        repo.save(u);

        // JWT subject = email
        return jwtService.generateToken(u.getEmail());
    }
}
