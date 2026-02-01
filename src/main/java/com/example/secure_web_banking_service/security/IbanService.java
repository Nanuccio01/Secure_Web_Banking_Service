package com.example.secure_web_banking_service.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class IbanService {

    private final SecureRandom rnd = new SecureRandom();

    // Genera un IBAN italiano valido (ITkkXaaaaaCCCCCxxxxxxxxxxxx)
    // Lunghezza IT = 27
    public String generateItalianIban() {
        // ABI (5 cifre), CAB (5 cifre), conto (12 caratteri alfanumerici)
        String abi = randomDigits(5);
        String cab = randomDigits(5);
        String account = randomAlnumUpper(12);

        // CIN (1 lettera) – per semplicità casuale (formato reale)
        char cin = (char) ('A' + rnd.nextInt(26));

        // BBAN: CIN + ABI + CAB + ACCOUNT (1 + 5 + 5 + 12 = 23)
        String bban = "" + cin + abi + cab + account;

        // Check digits: calcolo mod97 su (BBAN + IT00)
        String check = computeIbanCheckDigits("IT", bban);

        return "IT" + check + bban;
    }

    // Calcola check digits IBAN (ISO 13616)
    private String computeIbanCheckDigits(String countryCode, String bban) {
        String rearranged = bban + countryCode + "00";
        String numeric = toNumericString(rearranged);
        int mod = mod97(numeric);
        int checkDigits = 98 - mod;
        return (checkDigits < 10) ? ("0" + checkDigits) : String.valueOf(checkDigits);
    }

    private String toNumericString(String s) {
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (char ch : s.toCharArray()) {
            if (ch >= '0' && ch <= '9') {
                sb.append(ch);
            } else if (ch >= 'A' && ch <= 'Z') {
                sb.append((ch - 'A') + 10);
            } else if (ch >= 'a' && ch <= 'z') {
                sb.append((ch - 'a') + 10);
            } else {
                throw new IllegalArgumentException("Invalid IBAN char: " + ch);
            }
        }
        return sb.toString();
    }

    // mod97 su stringa numerica molto lunga (a blocchi)
    private int mod97(String numeric) {
        int mod = 0;
        for (int i = 0; i < numeric.length(); i++) {
            int digit = numeric.charAt(i) - '0';
            mod = (mod * 10 + digit) % 97;
        }
        return mod;
    }

    private String randomDigits(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String randomAlnumUpper(int n) {
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
