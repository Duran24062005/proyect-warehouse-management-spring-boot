const API_BASE = "http://localhost:8000/api";
const SESSIONS_KEY = "logiTrackSessions";
const ACTIVE_SESSION_KEY = "logiTrackActiveSession";

function parseSessions() {
    try {
        const raw = window.localStorage.getItem(SESSIONS_KEY);
        const parsed = raw ? JSON.parse(raw) : {};
        return parsed && typeof parsed === "object" ? parsed : {};
    } catch (_error) {
        return {};
    }
}

function saveSessions(sessions) {
    window.localStorage.setItem(SESSIONS_KEY, JSON.stringify(sessions));
}

function getActiveSessionEmail() {
    return window.localStorage.getItem(ACTIVE_SESSION_KEY);
}

function setActiveSession(email) {
    window.localStorage.setItem(ACTIVE_SESSION_KEY, email);
}

export function getSavedSessions() {
    return Object.values(parseSessions()).sort((left, right) => {
        return new Date(right.savedAt || 0) - new Date(left.savedAt || 0);
    });
}

export function getActiveSession() {
    const email = getActiveSessionEmail();
    if (!email) {
        return null;
    }
    return parseSessions()[email] || null;
}

export function saveSession(session) {
    const sessions = parseSessions();
    sessions[session.email] = {
        ...session,
        savedAt: new Date().toISOString()
    };
    saveSessions(sessions);
    setActiveSession(session.email);
    syncLegacyAuthStorage();
}

export function activateSavedSession(email) {
    const sessions = parseSessions();
    if (!sessions[email]) {
        return false;
    }
    setActiveSession(email);
    syncLegacyAuthStorage();
    return true;
}

export function removeSavedSession(email) {
    const sessions = parseSessions();
    delete sessions[email];
    saveSessions(sessions);

    if (getActiveSessionEmail() === email) {
        const nextSession = Object.values(sessions)[0] || null;
        if (nextSession) {
            setActiveSession(nextSession.email);
        } else {
            window.localStorage.removeItem(ACTIVE_SESSION_KEY);
        }
    }

    syncLegacyAuthStorage();
}

export function clearAllSessions() {
    window.localStorage.removeItem(SESSIONS_KEY);
    window.localStorage.removeItem(ACTIVE_SESSION_KEY);
    window.localStorage.removeItem("authToken");
    window.localStorage.removeItem("authUser");
}

export function syncLegacyAuthStorage() {
    const active = getActiveSession();
    if (!active) {
        window.localStorage.removeItem("authToken");
        window.localStorage.removeItem("authUser");
        return;
    }

    window.localStorage.setItem("authToken", active.token);
    window.localStorage.setItem("authUser", JSON.stringify(active.user));
}

export async function parseJsonSafe(response) {
    try {
        return await response.json();
    } catch (_error) {
        return {};
    }
}

export function mapApiError(data) {
    const details = Array.isArray(data.details) ? data.details : [];
    const message = data.message || "";

    const translatedMessages = {
        "Email already registered": "Ese correo ya está registrado. Usa otro o inicia sesión.",
        "Invalid credentials": "Correo o contraseña incorrectos.",
        "User is disabled": "Tu usuario está deshabilitado. Contacta al administrador.",
        "Current password is invalid": "La contraseña actual no es correcta.",
        "New password must be different": "La nueva contraseña debe ser distinta a la actual."
    };

    if (translatedMessages[message]) {
        return translatedMessages[message];
    }

    if (details.length > 0) {
        return details.join(" | ");
    }

    return message || "Ocurrió un error inesperado.";
}

export async function apiFetch(path, options = {}) {
    const session = getActiveSession();
    const headers = new Headers(options.headers || {});

    if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData)) {
        headers.set("Content-Type", "application/json");
    }

    if (session?.token) {
        headers.set("Authorization", `Bearer ${session.token}`);
    }

    const response = await window.fetch(`${API_BASE}${path}`, {
        ...options,
        headers
    });

    const data = await parseJsonSafe(response);

    if (!response.ok) {
        const error = new Error(mapApiError(data));
        error.status = response.status;
        error.payload = data;
        throw error;
    }

    return data;
}

export function ensureAuthenticated(redirectTo = "/index.html") {
    syncLegacyAuthStorage();
    if (!getActiveSession()) {
        window.location.href = redirectTo;
        return false;
    }
    return true;
}

export function logoutCurrentSession({ redirect = true } = {}) {
    const activeEmail = getActiveSessionEmail();
    if (activeEmail) {
        removeSavedSession(activeEmail);
    } else {
        clearAllSessions();
    }

    if (redirect) {
        window.location.href = "/index.html";
    }
}

export function formatRole(role) {
    if (!role) {
        return "Sin rol";
    }
    return role === "ADMIN" ? "Administrador" : "Usuario";
}

export { API_BASE };
