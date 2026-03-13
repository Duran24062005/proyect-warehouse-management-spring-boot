document.addEventListener("DOMContentLoaded", () => {
    const auth = window.logiTrackAuth;
    const loginForm = document.querySelector("#login-form");
    const registerForm = document.querySelector("#register-form");
    const banner = document.querySelector("[data-active-session-banner]");
    const savedSessionsContainer = document.querySelector("[data-saved-sessions]");

    renderActiveSessionBanner(auth, banner);
    renderSavedSessions(auth, savedSessionsContainer);
    wireDemoUsers();

    if (loginForm) {
        wireLoginForm(auth, loginForm, savedSessionsContainer, banner);
    }

    if (registerForm) {
        wireRegisterForm(auth, registerForm);
    }
});

const DEMO_USERS = [
    { email: "admin@logitrack.com", password: "Admin123!", label: "Admin demo", role: "ADMIN" },
    { email: "mlopez@logitrack.com", password: "User123!", label: "Operaciones", role: "USER" },
    { email: "jgarcia@logitrack.com", password: "User123!", label: "Inventario", role: "USER" },
    { email: "cperez@logitrack.com", password: "User123!", label: "Bodega", role: "USER" }
];

function wireLoginForm(auth, form, savedSessionsContainer, banner) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const feedback = form.querySelector("#form-feedback");
        const submitButton = form.querySelector("#submit-button");
        const payload = {
            email: form.email.value.trim(),
            password: form.password.value
        };

        clearFeedback(feedback);
        setSubmitting(submitButton, true, "Ingresando...");

        try {
            const response = await fetch(`${auth.API_BASE}/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await auth.parseJsonSafe(response);

            if (!response.ok) {
                throw new Error(auth.mapApiError(data));
            }

            auth.saveSession({
                email: data.user.email,
                token: data.token,
                user: data.user
            });

            showFeedback(feedback, data.message || "Login exitoso.", "success");
            renderActiveSessionBanner(auth, banner);
            renderSavedSessions(auth, savedSessionsContainer);

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

function wireRegisterForm(auth, form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const feedback = form.querySelector("#form-feedback");
        const submitButton = form.querySelector("#submit-button");
        const payload = {
            email: form.email.value.trim(),
            password: form.password.value,
            firstName: form.firstName.value.trim(),
            lastName: form.lastName.value.trim(),
            phoneNumber: form.phone.value.trim()
        };

        clearFeedback(feedback);
        setSubmitting(submitButton, true, "Registrando...");

        try {
            const response = await fetch(`${auth.API_BASE}/auth/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await auth.parseJsonSafe(response);

            if (!response.ok) {
                throw new Error(auth.mapApiError(data));
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

function renderSavedSessions(auth, container) {
    if (!container) {
        return;
    }

    const sessions = auth.getSavedSessions();
    const active = auth.getActiveSession();

    if (!sessions.length) {
        container.innerHTML = '<p class="saved-sessions-empty">Aún no hay sesiones guardadas. Cuando ingreses con varios usuarios podrás alternarlos desde aquí.</p>';
        return;
    }

    container.innerHTML = sessions.map((session) => {
        const isActive = active?.email === session.email;
        return `
            <article class="session-chip ${isActive ? "is-active" : ""}">
                <div>
                    <strong>${escapeHtml(session.user.firstName)} ${escapeHtml(session.user.lastName)}</strong>
                    <span>${escapeHtml(session.email)} · ${escapeHtml(auth.formatRole(session.user.role))}</span>
                </div>
                <div class="session-chip-actions">
                    <button type="button" data-session-activate="${escapeAttribute(session.email)}">${isActive ? "Activa" : "Usar"}</button>
                    <button type="button" class="button-danger" data-session-remove="${escapeAttribute(session.email)}">Cerrar</button>
                </div>
            </article>
        `;
    }).join("");

    container.querySelectorAll("[data-session-activate]").forEach((button) => {
        button.addEventListener("click", () => {
            auth.activateSavedSession(button.dataset.sessionActivate);
            window.location.href = "/platform/system.html";
        });
    });

    container.querySelectorAll("[data-session-remove]").forEach((button) => {
        button.addEventListener("click", () => {
            auth.removeSavedSession(button.dataset.sessionRemove);
            renderSavedSessions(auth, container);
            renderActiveSessionBanner(auth, document.querySelector("[data-active-session-banner]"));
        });
    });
}

function renderActiveSessionBanner(auth, banner) {
    if (!banner) {
        return;
    }

    const active = auth.getActiveSession();
    if (!active) {
        banner.classList.remove("is-visible");
        banner.innerHTML = "";
        return;
    }

    banner.classList.add("is-visible");
    banner.innerHTML = `
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

function setSubmitting(button, isSubmitting, text) {
    button.textContent = text;
    button.disabled = isSubmitting;
}

function showFeedback(container, message, type) {
    container.textContent = message;
    container.classList.add("is-visible");
    container.classList.toggle("is-success", type === "success");
    container.classList.toggle("is-error", type !== "success");
}

function clearFeedback(container) {
    container.textContent = "";
    container.classList.remove("is-visible", "is-success", "is-error");
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

function escapeAttribute(value) {
    return escapeHtml(value);
}
