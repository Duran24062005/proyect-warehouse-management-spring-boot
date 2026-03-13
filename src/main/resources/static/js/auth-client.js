(function () {
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

    function setActiveSession(email) {
        window.localStorage.setItem(ACTIVE_SESSION_KEY, email);
    }

    function getActiveSessionEmail() {
        return window.localStorage.getItem(ACTIVE_SESSION_KEY);
    }

    function getSavedSessions() {
        const sessions = parseSessions();
        return Object.values(sessions).sort((left, right) => {
            return new Date(right.savedAt || 0) - new Date(left.savedAt || 0);
        });
    }

    function getActiveSession() {
        const sessions = parseSessions();
        const activeEmail = getActiveSessionEmail();
        if (!activeEmail) {
            return null;
        }
        return sessions[activeEmail] || null;
    }

    function saveSession(session) {
        const sessions = parseSessions();
        sessions[session.email] = {
            ...session,
            savedAt: new Date().toISOString()
        };
        saveSessions(sessions);
        setActiveSession(session.email);
        window.localStorage.setItem("authToken", session.token);
        window.localStorage.setItem("authUser", JSON.stringify(session.user));
    }

    function syncLegacyAuthStorage() {
        const activeSession = getActiveSession();
        if (!activeSession) {
            window.localStorage.removeItem("authToken");
            window.localStorage.removeItem("authUser");
            return;
        }
        window.localStorage.setItem("authToken", activeSession.token);
        window.localStorage.setItem("authUser", JSON.stringify(activeSession.user));
    }

    function activateSavedSession(email) {
        const sessions = parseSessions();
        if (!sessions[email]) {
            return false;
        }
        setActiveSession(email);
        syncLegacyAuthStorage();
        return true;
    }

    function removeSavedSession(email) {
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

    function clearAllSessions() {
        window.localStorage.removeItem(SESSIONS_KEY);
        window.localStorage.removeItem(ACTIVE_SESSION_KEY);
        window.localStorage.removeItem("authToken");
        window.localStorage.removeItem("authUser");
    }

    async function parseJsonSafe(response) {
        try {
            return await response.json();
        } catch (_error) {
            return {};
        }
    }

    function mapApiError(data) {
        const details = Array.isArray(data.details) ? data.details : [];
        const message = data.message || "";

        if (message === "Email already registered") {
            return "Ese correo ya está registrado. Usa otro o inicia sesión.";
        }

        if (message === "Invalid credentials") {
            return "Correo o contraseña incorrectos.";
        }

        if (message === "User is disabled") {
            return "Tu usuario está deshabilitado. Contacta al administrador.";
        }

        if (message === "Current password is invalid") {
            return "La contraseña actual no es correcta.";
        }

        if (message === "New password must be different") {
            return "La nueva contraseña debe ser distinta a la actual.";
        }

        if (message === "Validation failed" && details.length > 0) {
            return details.join(" | ");
        }

        if (details.length > 0) {
            return details.join(" | ");
        }

        return message || "Ocurrió un error inesperado.";
    }

    async function apiFetch(path, options = {}) {
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

    function ensureAuthenticated(redirectTo = "/index.html") {
        syncLegacyAuthStorage();
        if (!getActiveSession()) {
            window.location.href = redirectTo;
            return false;
        }
        return true;
    }

    function logoutCurrentSession(options = {}) {
        const activeEmail = getActiveSessionEmail();
        if (activeEmail) {
            removeSavedSession(activeEmail);
        } else {
            clearAllSessions();
        }

        if (options.redirect !== false) {
            window.location.href = "/index.html";
        }
    }

    function formatRole(role) {
        if (!role) {
            return "Sin rol";
        }
        return role === "ADMIN" ? "Administrador" : "Usuario";
    }

    window.logiTrackAuth = {
        API_BASE,
        apiFetch,
        activateSavedSession,
        clearAllSessions,
        ensureAuthenticated,
        formatRole,
        getActiveSession,
        getSavedSessions,
        logoutCurrentSession,
        mapApiError,
        parseJsonSafe,
        removeSavedSession,
        saveSession,
        syncLegacyAuthStorage
    };
})();
