// Device ID applicativo persistente per questo browser
function getOrCreateDeviceId() {
    let id = localStorage.getItem("deviceId");
    if (!id) {
        // UUID (supportato dai browser moderni)
        id = crypto.randomUUID();
        localStorage.setItem("deviceId", id);
    }
    return id;
}
