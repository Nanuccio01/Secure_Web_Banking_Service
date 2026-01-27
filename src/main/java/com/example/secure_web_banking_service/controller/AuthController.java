package com.example.secure_web_banking_service.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.secure_web_banking_service.dto.*;
import com.example.secure_web_banking_service.model.AppUser;
import com.example.secure_web_banking_service.repository.UserRepository;
import com.example.secure_web_banking_service.security.JwtService;
import com.example.secure_web_banking_service.security.OtpService;
import com.example.secure_web_banking_service.security.AesService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AesService aesService;

    public AuthController(UserRepository repo,
                          PasswordEncoder encoder,
                          JwtService jwtService,
                          OtpService otpService,
                          AesService aesService) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.aesService = aesService;
    }

    // ------------------ REGISTER ------------------
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        AppUser u = new AppUser();
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setDeviceId(req.deviceId());
        repo.save(u);
        return "REGISTER_OK";
    }

    // ------------------ LOGIN ------------------
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {

        AppUser u = repo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Bad credentials");
        }

        if (!u.getDeviceId().equals(req.deviceId())) {
            throw new RuntimeException("Unrecognized device");
        }

        // Invia OTP all’utente dopo login corretto
        String otp = otpService.generateOtp(u.getUsername());

        // Qui puoi integrare invio via email/SMS
        System.out.println("OTP generata per login: " + otp);

        // JWT temporaneo opzionale se vuoi tracciare sessione preliminare
        return "OTP_SENT"; // risposta al client
    }

    // ------------------ VALIDAZIONE OTP E JWT DEFINITIVO ------------------
    @PostMapping("/validate-otp")
    public String validateOtp(@RequestBody OtpRequest req) {

        boolean valid = otpService.validateOtp(req.username(), req.otp());

        if (!valid) return "OTP_INVALID";

        // OTP valida → rilascia JWT definitivo
        AppUser u = repo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(u.getUsername(), u.getDeviceId());

        // Restituisce il token JWT al client
        return token;
    }

    // Endpoint per aggiornare saldo cifrato
    @PostMapping("/update-balance")
    public String updateBalance(@RequestParam String username, @RequestParam double amount) throws Exception {

        AppUser u = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // cifra il saldo come stringa
        String encrypted = aesService.encrypt(String.valueOf(amount));
        u.setEncryptedBalance(encrypted);
        repo.save(u);

        return "BALANCE_UPDATED";
    }

    // Endpoint per leggere saldo decifrato
    @GetMapping("/get-balance")
    public String getBalance(@RequestParam String username) throws Exception {

        AppUser u = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String decrypted = aesService.decrypt(u.getEncryptedBalance());
        return decrypted;
    }

    // ------------------ ENDPOINT PROTETTO ------------------
    @GetMapping("/secure")
    public String secureTest() {
        return "ACCESS_GRANTED";
    }
}
