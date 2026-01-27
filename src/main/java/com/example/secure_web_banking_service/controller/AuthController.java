package com.example.secure_web_banking_service.controller;

import com.example.secure_web_banking_service.dto.LoginRequest;
import com.example.secure_web_banking_service.dto.RegisterRequest;
import com.example.secure_web_banking_service.model.User;
import com.example.secure_web_banking_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest req) {
        User u = new User();
        u.setUsername(req.username());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setDeviceId(req.deviceId());
        repo.save(u);
        return "REGISTER_OK";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest req) {
        User u = repo.findByUsername(req.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Bad credentials");
        }

        if (!u.getDeviceId().equals(req.deviceId())) {
            throw new RuntimeException("Unrecognized device");
        }

        return "LOGIN_OK";
    }
}