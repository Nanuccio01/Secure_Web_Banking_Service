document.getElementById("loginBtn").addEventListener("click", async () => {
    const msg = document.getElementById("msg");
    const emailErr = document.getElementById("emailErr");
    const passwordErr = document.getElementById("passwordErr");

    // reset messaggi
    msg.textContent = "";
    emailErr.textContent = "";
    passwordErr.textContent = "";

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value;

    // deviceId SEMPRE definito
    const deviceId = (() => {
        try {
            if (typeof getOrCreateDeviceId === "function") return getOrCreateDeviceId();

            let id = localStorage.getItem("deviceId");
            if (!id) {
                id = (crypto && crypto.randomUUID)
                    ? crypto.randomUUID()
                    : (Date.now() + "-" + Math.random());
                localStorage.setItem("deviceId", id);
            }
            return id;
        } catch (e) {
            return String(Date.now());
        }
    })();

    try {
        const res = await fetch("/auth/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Device-Id": deviceId
            },
            body: JSON.stringify({ email, password })
        });

        //Rate limiter login
        if (res.status === 429) {
            msg.textContent = "Troppi tentativi. Riprova tra poco";
            return;
        }

        // validazione input
        if (res.status === 400) {
            const data = await res.json().catch(() => null);
            if (data && data.error === "VALIDATION_ERROR" && data.fields) {
                if (data.fields.email) emailErr.textContent = data.fields.email;
                if (data.fields.password) passwordErr.textContent = data.fields.password;
                msg.textContent = "Controlla i campi evidenziati";
                return;
            }
            msg.textContent = "Richiesta non valida";
            return;
        }

        // risposta testuale
        const text = await res.text();

        // login ok → JWT
        if (res.ok && text && text.length > 20) {
            document.cookie = `jwt=${text}; path=/`;
            window.location.href = "/home";
            return;
        }

        //credenziali errate
        msg.textContent = "Credenziali errate";
    } catch (e) {
        msg.textContent = "Errore di rete: " + (e && e.message ? e.message : e);
    }
});




