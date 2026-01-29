let token = ""; // JWT salvato dopo OTP

document.getElementById("loginBtn").onclick = login;
document.getElementById("validateOtpBtn").onclick = validateOtp;
document.getElementById("updateBalanceBtn").onclick = updateBalance;
document.getElementById("getBalanceBtn").onclick = getBalance;
document.getElementById("secureAccessBtn").onclick = secureAccess;

// ------------------ LOGIN ------------------
function login() {
    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const deviceId = document.getElementById("deviceId").value;

    fetch("/auth/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({username, password, deviceId})
    })
        .then(res => res.text())
        .then(data => document.getElementById("result").innerText = data)
        .catch(err => console.error(err));
}

// ------------------ VALIDAZIONE OTP ------------------
function validateOtp() {
    const username = document.getElementById("username").value;
    const otp = document.getElementById("otp").value;

    fetch("/auth/validate-otp", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({username, otp})
    })
        .then(res => res.text())
        .then(data => {
            document.getElementById("result").innerText = "JWT ricevuto: " + data;
            token = data; // salviamo JWT per richieste protette
        })
        .catch(err => console.error(err));
}

// ------------------ AGGIORNA SALDO ------------------
function updateBalance() {
    const username = document.getElementById("username").value;
    const amount = document.getElementById("amount").value;

    fetch(`/auth/update-balance?username=${username}&amount=${amount}`, {
        method: "POST",
        headers: {"Authorization": "Bearer " + token}
    })
        .then(res => res.text())
        .then(data => document.getElementById("result").innerText = data)
        .catch(err => console.error(err));
}

// ------------------ MOSTRA SALDO ------------------
function getBalance() {
    const username = document.getElementById("username").value;

    fetch(`/auth/get-balance?username=${username}`, {
        method: "GET",
        headers: {"Authorization": "Bearer " + token}
    })
        .then(res => res.text())
        .then(data => document.getElementById("result").innerText = "Saldo: " + data)
        .catch(err => console.error(err));
}

// ------------------ ENDPOINT PROTETTO ------------------
function secureAccess() {
    fetch("/auth/secure", {
        method: "GET",
        headers: {"Authorization": "Bearer " + token}
    })
        .then(res => res.text())
        .then(data => document.getElementById("result").innerText = data)
        .catch(err => console.error(err));
}
