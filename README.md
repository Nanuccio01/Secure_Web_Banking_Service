# Secure Web Banking Service

Requisiti:
- Docker Desktop

Avvio (Windows / macOS / Linux):
1) Aprire un terminale nella cartella del progetto
2) Eseguire:
   docker compose up --build

App:
- http://localhost:8080/register.html
- http://localhost:8080/login.html
- http://localhost:8080/home

Stop:
- docker compose down

Reset DB (cancella dati):
- docker compose down -v


FUNZIONALITà ON:

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

Database PostgreSQL