document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.querySelector("#login-form");
    const registerForm = document.querySelector("#register-form");

    if (loginForm) {
        wireLoginForm(loginForm);
    }

    if (registerForm) {
        wireRegisterForm(registerForm);
    }
});

function wireLoginForm(form) {
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
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await parseJsonSafe(response);

            if (!response.ok) {
                throw new Error(mapApiError(data));
            }

            showFeedback(feedback, data.message || "Login exitoso.", "success");
            window.localStorage.setItem("authUser", JSON.stringify(data.user));

            setTimeout(() => {
                window.location.href = "/system.html";
            }, 800);
        } catch (error) {
            showFeedback(feedback, error.message || "No fue posible iniciar sesión.", "error");
        } finally {
            setSubmitting(submitButton, false, "Login");
        }
    });
}

function wireRegisterForm(form) {
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
            const response = await fetch("/api/auth/register", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const data = await parseJsonSafe(response);

            if (!response.ok) {
                throw new Error(mapApiError(data));
            }

            showFeedback(feedback, "Registro exitoso. Ahora puedes iniciar sesión.", "success");
            form.reset();

            setTimeout(() => {
                window.location.href = "/index.html";
            }, 1200);
        } catch (error) {
            showFeedback(feedback, error.message || "No fue posible completar el registro.", "error");
        } finally {
            setSubmitting(submitButton, false, "Registrarme");
        }
    });
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

    if (message === "Validation failed" && details.length > 0) {
        return details.join(" | ");
    }

    if (details.length > 0) {
        return details.join(" | ");
    }

    return message || "Ocurrió un error inesperado.";
}

function setSubmitting(button, isSubmitting, text) {
    button.value = text;
    button.disabled = isSubmitting;
    button.classList.toggle("opacity-60", isSubmitting);
    button.classList.toggle("cursor-not-allowed", isSubmitting);
}

function showFeedback(container, message, type) {
    container.textContent = message;
    container.classList.remove("hidden", "border-red-400/40", "bg-red-500/10", "text-red-100", "border-emerald-400/40", "bg-emerald-500/10", "text-emerald-100");

    if (type === "success") {
        container.classList.add("border-emerald-400/40", "bg-emerald-500/10", "text-emerald-100");
        return;
    }

    container.classList.add("border-red-400/40", "bg-red-500/10", "text-red-100");
}

function clearFeedback(container) {
    container.textContent = "";
    container.classList.add("hidden");
    container.classList.remove("border-red-400/40", "bg-red-500/10", "text-red-100", "border-emerald-400/40", "bg-emerald-500/10", "text-emerald-100");
}
