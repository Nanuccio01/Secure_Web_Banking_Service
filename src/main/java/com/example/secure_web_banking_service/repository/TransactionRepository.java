package com.example.secure_web_banking_service.repository;

import com.example.secure_web_banking_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // movimenti dove l’utente è mittente o destinatario
    List<Transaction> findTop20ByFromIbanOrToIbanOrderByCreatedAtDesc(String fromIban, String toIban);
}


