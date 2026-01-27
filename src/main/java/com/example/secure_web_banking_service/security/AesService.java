package com.example.secure_web_banking_service.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Base64;
import java.security.SecureRandom;

// Servizio per cifrare e decifrare dati sensibili con AES-GCM
@Service
public class AesService {

    private SecretKey key;               // chiave segreta AES
    private final int IV_SIZE = 12;      // IV per GCM
    private final int TAG_LENGTH_BIT = 128;

    private final SecureRandom random = new SecureRandom();

    // Inizializza chiave AES
    @PostConstruct
    public void init() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256); // AES-256
        key = generator.generateKey();
    }

    // ------------------ CIFRA ------------------
    public String encrypt(String data) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] encrypted = cipher.doFinal(data.getBytes());
        byte[] encryptedIvAndData = new byte[iv.length + encrypted.length];

        System.arraycopy(iv, 0, encryptedIvAndData, 0, iv.length);
        System.arraycopy(encrypted, 0, encryptedIvAndData, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(encryptedIvAndData);
    }

    // ------------------ DECIFRA ------------------
    public String decrypt(String encryptedData) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);

        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[decoded.length - IV_SIZE];

        System.arraycopy(decoded, 0, iv, 0, IV_SIZE);
        System.arraycopy(decoded, IV_SIZE, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted);
    }
}
