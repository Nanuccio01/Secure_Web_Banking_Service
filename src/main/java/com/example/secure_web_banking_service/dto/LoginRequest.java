package com.example.secure_web_banking_service.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(

        @NotBlank(message = "Email obbligatoria")
        @Email(message = "Email non valida")
        @Size(max = 120, message = "Email troppo lunga")
        String email,

        @NotBlank(message = "Password obbligatoria")
        @Size(min = 1, max = 128, message = "Password non valida")
        String password
) {}
