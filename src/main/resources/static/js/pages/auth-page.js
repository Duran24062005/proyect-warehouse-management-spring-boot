import {
    API_BASE,
    activateSavedSession,
    formatRole,
    getActiveSession,
    getSavedSessions,
    mapApiError,
    parseJsonSafe,
    removeSavedSession,
    saveSession
} from "../core/auth.js";
import { DEMO_USERS } from "../core/demo-users.js";
import { escapeAttribute, escapeHtml } from "../core/ui.js";

document.addEventListener("DOMContentLoaded", () => {
    const page = document.body.dataset.page;
    const loginForm = document.querySelector("#login-form");
    const registerForm = document.querySelector("#register-form");
    const banner = document.querySelector("[data-active-session-banner]");
    const sessionsContainer = document.querySelector("[data-saved-sessions]");

    renderActiveSessionBanner(banner);
    renderSavedSessions(sessionsContainer);
    wireDemoUsers();

    if (page === "login" && loginForm) {
        wireLoginForm(loginForm, banner, sessionsContainer);
    }

    if (page === "register" && registerForm) {
        wireRegisterForm(registerForm);
    }
});

function wireLoginForm(form, banner, sessionsContainer) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const feedback = form.querySelector("[data-form-feedback]");
        const submitButton = form.querySelector("[data-submit-button]");
        const payload = {
            email: form.email.value.trim(),
            password: form.password.value
        };

        setSubmitting(submitButton, true, "Ingresando...");
        clearFeedback(feedback);

        try {
            const response = await window.fetch(`${API_BASE}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            const data = await parseJsonSafe(response);
            if (!response.ok) {
                throw new Error(mapApiError(data));
            }

            saveSession({
                email: data.user.email,
                token: data.token,
                user: data.user
            });

            showFeedback(feedback, data.message || "Login exitoso.", "success");
            renderActiveSessionBanner(banner);
            renderSavedSessions(sessionsContainer);

            window.setTimeout(() => {
                window.location.href = "/platform/system.html";
            }, 700);
        } catch (error) {
            showFeedback(feedback, error.message || "No fue posible iniciar sesión.", "error");
        } finally {
            setSubmitting(submitButton, false, "Entrar al sistema");
        }
    });
}

function wireRegisterForm(form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const feedback = form.querySelector("[data-form-feedback]");
        const submitButton = form.querySelector("[data-submit-button]");
        const payload = {
            email: form.email.value.trim(),
            password: form.password.value,
            firstName: form.firstName.value.trim(),
            lastName: form.lastName.value.trim(),
            phoneNumber: form.phoneNumber.value.trim()
        };

        setSubmitting(submitButton, true, "Registrando...");
        clearFeedback(feedback);

        try {
            const response = await window.fetch(`${API_BASE}/auth/register`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            const data = await parseJsonSafe(response);
            if (!response.ok) {
                throw new Error(mapApiError(data));
            }

            showFeedback(feedback, "Registro exitoso. Ahora puedes iniciar sesión.", "success");
            form.reset();

            window.setTimeout(() => {
                window.location.href = "/index.html";
            }, 1000);
        } catch (error) {
            showFeedback(feedback, error.message || "No fue posible completar el registro.", "error");
        } finally {
            setSubmitting(submitButton, false, "Crear cuenta");
        }
    });
}

function renderSavedSessions(container) {
    if (!container) {
        return;
    }

    const sessions = getSavedSessions();
    const active = getActiveSession();

    if (!sessions.length) {
        container.innerHTML = '<p class="saved-sessions-empty">Aún no hay sesiones guardadas. Cuando ingreses con varios usuarios podrás alternarlos desde aquí.</p>';
        return;
    }

    container.innerHTML = sessions.map((session) => {
        const isActive = session.email === active?.email;
        return `
            <article class="session-chip ${isActive ? "is-active" : ""}">
                <div>
                    <strong>${escapeHtml(session.user.firstName)} ${escapeHtml(session.user.lastName)}</strong>
                    <span>${escapeHtml(session.email)} · ${escapeHtml(formatRole(session.user.role))}</span>
                </div>
                <div class="session-chip-actions">
                    <button class="button-secondary" type="button" data-session-activate="${escapeAttribute(session.email)}">${isActive ? "Activa" : "Usar"}</button>
                    <button class="button-danger" type="button" data-session-remove="${escapeAttribute(session.email)}">Cerrar</button>
                </div>
            </article>
        `;
    }).join("");

    container.querySelectorAll("[data-session-activate]").forEach((button) => {
        button.addEventListener("click", () => {
            activateSavedSession(button.dataset.sessionActivate);
            window.location.href = "/platform/system.html";
        });
    });

    container.querySelectorAll("[data-session-remove]").forEach((button) => {
        button.addEventListener("click", () => {
            removeSavedSession(button.dataset.sessionRemove);
            renderSavedSessions(container);
            renderActiveSessionBanner(document.querySelector("[data-active-session-banner]"));
        });
    });
}

function renderActiveSessionBanner(container) {
    if (!container) {
        return;
    }

    const active = getActiveSession();
    if (!active) {
        container.classList.remove("is-visible");
        container.innerHTML = "";
        return;
    }

    container.classList.add("is-visible");
    container.innerHTML = `
        <p>Sesión activa: <strong>${escapeHtml(active.user.firstName)} ${escapeHtml(active.user.lastName)}</strong> · ${escapeHtml(active.email)}</p>
        <a class="button-secondary" href="/platform/system.html">Continuar</a>
    `;
}

function wireDemoUsers() {
    document.querySelectorAll("[data-demo-login]").forEach((button) => {
        button.addEventListener("click", () => {
            const user = DEMO_USERS.find((candidate) => candidate.email === button.dataset.demoLogin);
            const form = document.querySelector("#login-form");

            if (!user || !form) {
                return;
            }

            form.email.value = user.email;
            form.password.value = user.password;
            form.email.focus();
        });
    });
}

function setSubmitting(button, isSubmitting, label) {
    button.disabled = isSubmitting;
    button.textContent = label;
}

function clearFeedback(container) {
    container.textContent = "";
    container.classList.remove("is-visible", "is-success", "is-error");
}

function showFeedback(container, message, type) {
    container.textContent = message;
    container.classList.add("is-visible");
    container.classList.toggle("is-success", type === "success");
    container.classList.toggle("is-error", type !== "success");
}
