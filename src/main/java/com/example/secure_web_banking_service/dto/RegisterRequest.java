package com.example.secure_web_banking_service.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(

        @NotBlank(message = "Nome obbligatorio")
        @Size(min = 2, max = 50, message = "Nome: 2-50 caratteri")
        String firstName,

        @NotBlank(message = "Cognome obbligatorio")
        @Size(min = 2, max = 50, message = "Cognome: 2-50 caratteri")
        String lastName,

        @NotBlank(message = "Email obbligatoria")
        @Email(message = "Email non valida")
        @Size(max = 120, message = "Email troppo lunga")
        String email,

        @NotBlank(message = "Telefono obbligatorio")
        @Pattern(regexp = "^[0-9+ ]{8,20}$", message = "Telefono non valido")
        String phone,

        @NotBlank(message = "Password obbligatoria")
        @Size(min = 12, max = 64, message = "Password: 12-64 caratteri")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-={}\\[\\]:;\"'<>,.\\/\\\\|~`]).{12,64}$",
                message = "Password: almeno 1 maiuscola, 1 minuscola, 1 numero, 1 speciale"
        )
        String password
) {}
