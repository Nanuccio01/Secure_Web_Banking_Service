package com.example.secure_web_banking_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.secure_web_banking_service.model.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<AppUser> findByIban(String iban);
    boolean existsByIban(String iban);
}


