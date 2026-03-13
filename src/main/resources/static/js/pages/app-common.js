import {
    activateSavedSession,
    ensureAuthenticated,
    formatRole,
    getActiveSession,
    getSavedSessions,
    logoutCurrentSession,
    removeSavedSession
} from "../core/auth.js";
import { escapeAttribute, escapeHtml, showNotice } from "../core/ui.js";

export function requireSession() {
    return ensureAuthenticated();
}

export function wireGlobalAppActions() {
    document.querySelectorAll("[data-logout]").forEach((button) => {
        button.addEventListener("click", () => logoutCurrentSession());
    });

    document.querySelectorAll("[data-refresh-page]").forEach((button) => {
        button.addEventListener("click", () => window.location.reload());
    });
}

export function setActiveNavigation(page) {
    document.querySelectorAll("[data-nav]").forEach((link) => {
        link.classList.toggle("is-active", link.dataset.nav === page);
    });
}

export function renderSidebarSession(user) {
    const container = document.querySelector("[data-session-card]");
    if (!container) {
        return;
    }

    container.innerHTML = `
        <span class="tag">Sesion Activa</span>
        <h3>${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</h3>
        <p>${escapeHtml(user.email)}</p>
        <div class="session-badges">
            <span class="badge badge--accent">${escapeHtml(formatRole(user.role))}</span>
            <span class="badge badge--soft">${user.enabled ? "Habilitado" : "Deshabilitado"}</span>
        </div>
    `;
}

export function renderSessionSwitcher() {
    const container = document.querySelector("[data-session-switcher]");
    if (!container) {
        return;
    }

    const sessions = getSavedSessions();
    const active = getActiveSession();

    if (!sessions.length) {
        container.innerHTML = '<p class="empty-state">No hay sesiones guardadas.</p>';
        return;
    }

    container.innerHTML = sessions.map((session) => `
        <article class="session-row ${session.email === active?.email ? "is-active" : ""}">
            <div>
                <strong>${escapeHtml(session.user.firstName)} ${escapeHtml(session.user.lastName)}</strong>
                <div class="list-meta">${escapeHtml(session.email)} · ${escapeHtml(formatRole(session.user.role))}</div>
            </div>
            <div class="session-row-actions">
                <button class="button-secondary" type="button" data-switch-session="${escapeAttribute(session.email)}">${session.email === active?.email ? "Activa" : "Cambiar"}</button>
                <button class="button-danger" type="button" data-remove-session="${escapeAttribute(session.email)}">Cerrar</button>
            </div>
        </article>
    `).join("");

    container.querySelectorAll("[data-switch-session]").forEach((button) => {
        button.addEventListener("click", () => {
            activateSavedSession(button.dataset.switchSession);
            window.location.reload();
        });
    });

    container.querySelectorAll("[data-remove-session]").forEach((button) => {
        button.addEventListener("click", () => {
            removeSavedSession(button.dataset.removeSession);
            if (!getActiveSession()) {
                window.location.href = "/index.html";
                return;
            }
            renderSessionSwitcher();
        });
    });
}

export function guardAdminView(user) {
    const adminNodes = document.querySelectorAll("[data-admin-only]");
    adminNodes.forEach((node) => {
        node.classList.toggle("is-hidden", user.role !== "ADMIN");
    });
}

export function handleAppError(error, noticeContainer) {
    if (error?.status === 401) {
        logoutCurrentSession({ redirect: false });
        window.location.href = "/index.html";
        return;
    }

    showNotice(noticeContainer, error.message || "No fue posible cargar la información.", "error");
}
