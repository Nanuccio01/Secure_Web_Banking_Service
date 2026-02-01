document.getElementById("registerBtn").addEventListener("click", async () => {
    const msg = document.getElementById("msg");

    const firstNameErr = document.getElementById("firstNameErr");
    const lastNameErr = document.getElementById("lastNameErr");
    const emailErr = document.getElementById("emailErr");
    const phoneErr = document.getElementById("phoneErr");
    const passwordErr = document.getElementById("passwordErr");

    // reset
    msg.textContent = "";
    firstNameErr.textContent = "";
    lastNameErr.textContent = "";
    emailErr.textContent = "";
    phoneErr.textContent = "";
    passwordErr.textContent = "";

    const firstName = document.getElementById("firstName").value.trim();
    const lastName  = document.getElementById("lastName").value.trim();
    const email     = document.getElementById("email").value.trim();
    const phone     = document.getElementById("phone").value.trim();
    const password  = document.getElementById("password").value;

    const deviceId = (() => {
        try {
            // se device.js è caricato
            if (typeof getOrCreateDeviceId === "function") return getOrCreateDeviceId();

            // fallback senza device.js
            let id = localStorage.getItem("deviceId");
            if (!id) {
                id = (crypto && crypto.randomUUID) ? crypto.randomUUID() : (Date.now() + "-" + Math.random());
                localStorage.setItem("deviceId", id);
            }
            return id;
        } catch (e) {
            return String(Date.now());
        }
    })();

    try {
        const res = await fetch("/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Device-Id": deviceId
            },
            body: JSON.stringify({ firstName, lastName, email, phone, password })
        });

        if (res.status === 400) {
            const data = await res.json().catch(() => null);
            if (data && data.error === "VALIDATION_ERROR" && data.fields) {
                if (data.fields.firstName) firstNameErr.textContent = data.fields.firstName;
                if (data.fields.lastName) lastNameErr.textContent = data.fields.lastName;
                if (data.fields.email) emailErr.textContent = data.fields.email;
                if (data.fields.phone) phoneErr.textContent = data.fields.phone;
                if (data.fields.password) passwordErr.textContent = data.fields.password;
                msg.textContent = "Controlla i campi evidenziati.";
                return;
            }
            msg.textContent = "Richiesta non valida.";
            return;
        }

        // OK/Errore: testo
        const text = await res.text();

        if (res.ok && text === "REGISTER_OK") {
            window.location.href = "/login.html";
            return;
        }

        if (text === "EMAIL_TAKEN") {
            msg.textContent = "Email già in uso.";
            return;
        }

        msg.textContent = "Errore registrazione.";
    } catch (e) {
        msg.textContent = "Errore di rete: " + (e && e.message ? e.message : e);
    }
});
