package com.example.secure_web_banking_service.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// Servizio per generare e validare OTP
@Service
public class OtpService {

    private final Map<String, OtpEntry> otpStore = new HashMap<>();
    private final Random random = new Random();
    private final long EXPIRATION_MS = 5 * 60 * 1000; // 5 minuti

    // Genera OTP e la memorizza in memoria
    public String generateOtp(String username) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(username, new OtpEntry(otp, Instant.now().plusMillis(EXPIRATION_MS)));

        // Per test, stampiamo OTP in console
        System.out.println("OTP per " + username + ": " + otp);

        return otp;
    }

    // Valida OTP e controlla scadenza
    public boolean validateOtp(String username, String otp) {
        OtpEntry entry = otpStore.get(username);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiry)) {
            otpStore.remove(username);
            return false;
        }
        boolean valid = entry.otp.equals(otp);
        if (valid) otpStore.remove(username);
        return valid;
    }

    // Classe interna per salvare OTP + scadenza
    private static class OtpEntry {
        String otp;
        Instant expiry;
        OtpEntry(String otp, Instant expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
