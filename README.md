FUNZIONALITA' ON

Autenticazione e identità:
- Registrazione con: nome, cognome, email, telefono, password.

- Login con email + password.

- Password salvata come hash (BCrypt).

- Sessione gestita con JWT salvato in cookie jwt.

- Device binding “applicativo”: il browser genera un deviceId persistente (localStorage) e lo invia in header X-Device-Id; il backend lo salva su utente.

Dati e cifratura:
- IBAN fake in formato italiano realistico, generato alla registrazione e salvato in DB.

- Saldo salvato cifrato AES (e letto/mostrato decifrato).

- PII (nome/cognome/telefono) cifrati AES; email in chiaro per login.

Operazioni bancarie:
- Bonifico interno tramite IBAN.

- Rate Limiter login e bonifici.

- Aggiornamento saldo mittente/destinatario.

- Storico movimenti (ultimi 20), visibile sempre e auto-aggiornato.


UI Bootstrap