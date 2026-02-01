package com.example.secure_web_banking_service.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // IBAN mittente
    @Column(nullable = false, length = 27)
    private String fromIban;

    // IBAN destinatario
    @Column(nullable = false, length = 27)
    private String toIban;

    // importo (uso BigDecimal per soldi)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // causale opzionale
    @Column(length = 140)
    private String memo;

    // data/ora
    @Column(nullable = false)
    private Instant createdAt;

    public Long getId() { return id; }

    public String getFromIban() { return fromIban; }
    public void setFromIban(String fromIban) { this.fromIban = fromIban; }

    public String getToIban() { return toIban; }
    public void setToIban(String toIban) { this.toIban = toIban; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
