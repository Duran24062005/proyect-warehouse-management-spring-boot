import { apiFetch } from "../core/auth.js";
import { escapeHtml, renderTable, showNotice } from "../core/ui.js";
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
    setActiveNavigation("admin");
    wireGlobalAppActions();

    try {
        const me = await apiFetch("/auth/me");

        renderSidebarSession(me);
        renderSessionSwitcher();
        guardAdminView(me);

        if (me.role !== "ADMIN") {
            window.location.href = "/platform/system.html";
            return;
        }

        const [users, warehouses] = await Promise.all([
            apiFetch("/users"),
            apiFetch("/warehouses")
        ]);

        renderUsers(users);
        populateRoleSelect();
        populateWarehouseSelects(warehouses);
        populateManagerSelect(users, me);
        wireUserForm(notice);
        wireWarehouseManagerForm(warehouses, notice);
    } catch (error) {
        handleAppError(error, notice);
    }
});

function renderUsers(users) {
    document.querySelector("[data-page-heading]").textContent = "Consola administrativa";
    document.querySelector("[data-page-lead]").textContent = "Centraliza la creación de usuarios y la asignación de managers para mantener la estructura operativa limpia y controlada.";

    document.querySelector("[data-users-table]").innerHTML = renderTable(
        [
            { label: "Nombre", render: (row) => `${escapeHtml(row.firstName)} ${escapeHtml(row.lastName)}` },
            { label: "Correo", render: (row) => escapeHtml(row.email) },
            { label: "Rol", render: (row) => escapeHtml(row.role) },
            { label: "Estado", render: (row) => (row.enabled ? "Activo" : "Deshabilitado") }
        ],
        users
    );
}

function populateRoleSelect() {
    const select = document.querySelector('[data-create-user-form] select[name="role"]');
    select.innerHTML = `
        <option value="USER">USER</option>
        <option value="ADMIN">ADMIN</option>
    `;
}

function populateWarehouseSelects(warehouses) {
    const select = document.querySelector('[data-assign-manager-form] select[name="warehouseId"]');
    select.innerHTML = `
        <option value="">Selecciona una bodega</option>
        ${warehouses.map((warehouse) => `
            <option value="${warehouse.id}">${escapeHtml(warehouse.name)}</option>
        `).join("")}
    `;
}

function populateManagerSelect(users, me) {
    const select = document.querySelector('[data-assign-manager-form] select[name="managerUserId"]');
    select.innerHTML = `
        <option value="">Selecciona un manager</option>
        ${users.filter((user) => user.enabled).map((user) => `
            <option value="${user.id}" ${user.email === me.email ? "selected" : ""}>
                ${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)} · ${escapeHtml(user.email)}
            </option>
        `).join("")}
    `;
}

function wireUserForm(notice) {
    const form = document.querySelector("[data-create-user-form]");
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            email: form.email.value.trim(),
            password: form.password.value,
            firstName: form.firstName.value.trim(),
            lastName: form.lastName.value.trim(),
            phoneNumber: form.phoneNumber.value.trim(),
            role: form.role.value,
            enabled: form.enabled.checked
        };

        try {
            await apiFetch("/users", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Usuario creado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handleAppError(error, notice);
        }
    });
}

function wireWarehouseManagerForm(warehouses, notice) {
    const form = document.querySelector("[data-assign-manager-form]");
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const warehouseId = Number(form.warehouseId.value);
        const managerUserId = Number(form.managerUserId.value);
        const warehouse = warehouses.find((candidate) => candidate.id === warehouseId);

        if (!warehouse) {
            showNotice(notice, "Selecciona una bodega válida.", "error");
            return;
        }

        const payload = {
            name: warehouse.name,
            ubication: warehouse.ubication,
            capacity: warehouse.capacity,
            managerUserId
        };

        try {
            await apiFetch(`/warehouses/${warehouseId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Manager asignado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handleAppError(error, notice);
        }
    });
}
