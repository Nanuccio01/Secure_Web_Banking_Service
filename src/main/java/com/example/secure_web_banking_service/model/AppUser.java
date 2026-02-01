package com.example.secure_web_banking_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users") // evita "user" (riservato)
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nome/cognome cifrati AES (Base64
    @Column(nullable = false, length = 2048)
    private String encryptedFirstName;
    @Column(nullable = false, length = 2048)
    private String encryptedLastName;

    @Column(unique = true, nullable = false, length = 120)
    private String email;

    // telefono cifrato AES (Base64)
    @Column(nullable = false, length = 2048)
    private String encryptedPhone;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String deviceId;

    @Column(unique = true, nullable = false, length = 27)
    private String iban;

    // saldo cifrato AES (stringa Base64)
    @Column(length = 2048)
    private String encryptedBalance;

    public Long getId() { return id; }


    // getter/setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getEncryptedFirstName() { return encryptedFirstName; }
    public void setEncryptedFirstName(String v) { this.encryptedFirstName = v; }

    public String getEncryptedLastName() { return encryptedLastName; }
    public void setEncryptedLastName(String v) { this.encryptedLastName = v; }

    public String getEncryptedPhone() { return encryptedPhone; }
    public void setEncryptedPhone(String v) { this.encryptedPhone = v; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getEncryptedBalance() { return encryptedBalance; }
    public void setEncryptedBalance(String encryptedBalance) { this.encryptedBalance = encryptedBalance; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }
}

