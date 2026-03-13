import { apiFetch } from "../core/auth.js";
import { escapeHtml, showNotice } from "../core/ui.js";
import {
    guardAdminView,
    handleAppError,
    renderSessionSwitcher,
    renderSidebarSession,
    requireSession,
    setActiveNavigation,
    wireGlobalAppActions
} from "./app-common.js";

document.addEventListener("DOMContentLoaded", async () => {
    if (!requireSession()) {
        return;
    }

    const notice = document.querySelector("[data-page-notice]");
    setActiveNavigation("profile");
    wireGlobalAppActions();

    try {
        const me = await apiFetch("/auth/me");
        renderSidebarSession(me);
        renderSessionSwitcher();
        guardAdminView(me);
        renderProfile(me);
        wirePasswordForm(notice);
    } catch (error) {
        handleAppError(error, notice);
    }
});

function renderProfile(user) {
    document.querySelector("[data-page-heading]").textContent = "Tu cuenta y seguridad";
    document.querySelector("[data-page-lead]").textContent = "Gestiona tu sesión actual y actualiza tu contraseña sin salir del entorno operativo.";

    const items = [
        { label: "Nombre", value: `${user.firstName} ${user.lastName}` },
        { label: "Correo", value: user.email },
        { label: "Teléfono", value: user.phoneNumber },
        { label: "Rol", value: user.role },
        { label: "Estado", value: user.enabled ? "Habilitado" : "Deshabilitado" },
        { label: "Identificador", value: user.id }
    ];

    document.querySelector("[data-profile-grid]").innerHTML = items.map((item) => `
        <article class="profile-item">
            <span>${escapeHtml(item.label)}</span>
            <strong>${escapeHtml(item.value)}</strong>
        </article>
    `).join("");
}

function wirePasswordForm(notice) {
    const form = document.querySelector("[data-password-form]");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            currentPassword: form.currentPassword.value,
            newPassword: form.newPassword.value
        };

        try {
            const response = await apiFetch("/auth/change-password", {
                method: "PATCH",
                body: JSON.stringify(payload)
            });

            showNotice(notice, response.message || "Contraseña actualizada correctamente.", "success");
            form.reset();
        } catch (error) {
            handleAppError(error, notice);
        }
    });
}
