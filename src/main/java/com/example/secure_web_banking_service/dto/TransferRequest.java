package com.example.secure_web_banking_service.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "IBAN destinatario obbligatorio")
        @Size(min = 15, max = 34, message = "IBAN non valido")
        @Pattern(regexp = "^[A-Z]{2}\\d{2}[A-Z0-9]{11,30}$", message = "Formato IBAN non valido")
        String toIban,

        @NotNull(message = "Importo obbligatorio")
        @DecimalMin(value = "0.01", message = "Importo minimo 0.01")
        @Digits(integer = 16, fraction = 2, message = "Importo non valido")
        BigDecimal amount,

        @Size(max = 140, message = "Causale max 140 caratteri")
        String memo
) {}
