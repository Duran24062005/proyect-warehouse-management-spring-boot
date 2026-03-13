document.addEventListener("DOMContentLoaded", async () => {
    const auth = window.logiTrackAuth;

    if (!auth.ensureAuthenticated()) {
        return;
    }

    const page = document.body.dataset.page;
    const notice = document.querySelector("[data-page-notice]");

    wireCommonActions(auth, notice);
    renderSessionSwitcher(auth);

    try {
        const me = await auth.apiFetch("/auth/me");
        renderCurrentUser(me);

        if (page === "profile") {
            renderProfile(me);
            wirePasswordForm(auth, notice);
            return;
        }

        await renderDashboard(auth, me);
    } catch (error) {
        handlePageError(auth, error, notice);
    }
});

function wireCommonActions(auth, notice) {
    document.querySelector("[data-logout]")?.addEventListener("click", () => {
        auth.logoutCurrentSession();
    });

    document.querySelector("[data-refresh-dashboard]")?.addEventListener("click", () => {
        showNotice(notice, "Actualizando datos del dashboard...", "success");
        window.location.reload();
    });
}

function renderSessionSwitcher(auth) {
    const container = document.querySelector("[data-session-list]");
    if (!container) {
        return;
    }

    const sessions = auth.getSavedSessions();
    const active = auth.getActiveSession();

    if (!sessions.length) {
        container.innerHTML = '<p class="empty-state">No hay más sesiones guardadas en este navegador.</p>';
        return;
    }

    container.innerHTML = sessions.map((session) => {
        const isActive = active?.email === session.email;
        return `
            <article class="session-item ${isActive ? "is-active" : ""}">
                <div class="session-meta">
                    <strong>${escapeHtml(session.user.firstName)} ${escapeHtml(session.user.lastName)}</strong>
                    <span>${escapeHtml(session.email)} · ${escapeHtml(auth.formatRole(session.user.role))}</span>
                </div>
                <div class="session-actions">
                    <button class="button-link" type="button" data-session-switch="${escapeAttribute(session.email)}">${isActive ? "Activa" : "Cambiar"}</button>
                    <button class="button-danger" type="button" data-session-remove="${escapeAttribute(session.email)}">Cerrar</button>
                </div>
            </article>
        `;
    }).join("");

    container.querySelectorAll("[data-session-switch]").forEach((button) => {
        button.addEventListener("click", () => {
            auth.activateSavedSession(button.dataset.sessionSwitch);
            window.location.reload();
        });
    });

    container.querySelectorAll("[data-session-remove]").forEach((button) => {
        button.addEventListener("click", () => {
            auth.removeSavedSession(button.dataset.sessionRemove);
            if (!auth.getActiveSession()) {
                window.location.href = "/index.html";
                return;
            }
            renderSessionSwitcher(auth);
        });
    });
}

async function renderDashboard(auth, me) {
    const [products, lowStock, warehouses, movements] = await Promise.all([
        auth.apiFetch("/products"),
        auth.apiFetch("/products/low-stock"),
        auth.apiFetch("/warehouses"),
        auth.apiFetch("/movements")
    ]);

    renderMetrics({ products, lowStock, warehouses, movements, me });
    renderProducts(products);
    renderLowStock(lowStock);
    renderWarehouses(warehouses);
    renderMovements(movements);

    if (me.role === "ADMIN") {
        const users = await auth.apiFetch("/users");
        renderUsers(users);
        initAdminTools(auth, { users, warehouses }, me);
    }
}

function renderCurrentUser(user) {
    const container = document.querySelector("[data-current-user]");
    if (!container) {
        return;
    }

    container.innerHTML = `
        <div class="hero-user">
            <span class="user-pill">${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</span>
            <span class="role-pill">${escapeHtml(window.logiTrackAuth.formatRole(user.role))}</span>
            <span class="status-pill">${user.enabled ? "Usuario habilitado" : "Usuario deshabilitado"}</span>
        </div>
        <p class="app-subtle">${escapeHtml(user.email)} · ${escapeHtml(user.phoneNumber)}</p>
    `;
}

function renderMetrics({ products, lowStock, warehouses, movements, me }) {
    const container = document.querySelector("[data-metrics]");
    if (!container) {
        return;
    }

    const metrics = [
        { label: "Productos", value: products.length },
        { label: "Stock bajo", value: lowStock.length },
        { label: "Almacenes", value: warehouses.length },
        { label: me.role === "ADMIN" ? "Rol activo" : "Movimientos", value: me.role === "ADMIN" ? "ADMIN" : movements.length }
    ];

    container.innerHTML = metrics.map((metric) => `
        <article class="metric-card">
            <span class="metric-label">${escapeHtml(metric.label)}</span>
            <strong class="metric-value">${escapeHtml(metric.value)}</strong>
        </article>
    `).join("");
}

function renderProducts(products) {
    const container = document.querySelector("[data-products-table]");
    if (!container) {
        return;
    }

    if (!products.length) {
        container.innerHTML = '<p class="empty-state">No hay productos registrados.</p>';
        return;
    }

    container.innerHTML = `
        <table class="list-table">
            <thead>
                <tr>
                    <th>Producto</th>
                    <th>Categoría</th>
                    <th>Precio</th>
                    <th>Almacén</th>
                </tr>
            </thead>
            <tbody>
                ${products.slice(0, 8).map((product) => `
                    <tr>
                        <td>${escapeHtml(product.name)}</td>
                        <td>${escapeHtml(product.category)}</td>
                        <td>${formatCurrency(product.price)}</td>
                        <td>${escapeHtml(product.warehouseName || "Sin asignar")}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function renderLowStock(lowStock) {
    const container = document.querySelector("[data-low-stock]");
    if (!container) {
        return;
    }

    if (!lowStock.length) {
        container.innerHTML = '<p class="empty-state">No hay alertas de stock bajo en este momento.</p>';
        return;
    }

    container.innerHTML = lowStock.map((product) => `
        <article class="alert-item">
            <div>
                <strong>${escapeHtml(product.name)}</strong>
                <span>${escapeHtml(product.category)} · ${escapeHtml(product.warehouseName || "Sin almacén")}</span>
            </div>
            <span class="status-pill">Stock crítico</span>
        </article>
    `).join("");
}

function renderWarehouses(warehouses) {
    const container = document.querySelector("[data-warehouses-list]");
    if (!container) {
        return;
    }

    if (!warehouses.length) {
        container.innerHTML = '<p class="empty-state">No hay almacenes disponibles.</p>';
        return;
    }

    container.innerHTML = warehouses.slice(0, 6).map((warehouse) => `
        <article class="quick-item">
            <div>
                <strong>${escapeHtml(warehouse.name)}</strong>
                <span>${escapeHtml(warehouse.ubication)} · Manager: ${escapeHtml(warehouse.managerName || "Sin asignar")}</span>
            </div>
            <span class="status-pill">${escapeHtml(warehouse.capacity ?? "0")} cap.</span>
        </article>
    `).join("");
}

function renderMovements(movements) {
    const container = document.querySelector("[data-movements-table]");
    if (!container) {
        return;
    }

    if (!movements.length) {
        container.innerHTML = '<p class="empty-state">No hay movimientos para mostrar.</p>';
        return;
    }

    container.innerHTML = `
        <table class="list-table">
            <thead>
                <tr>
                    <th>Tipo</th>
                    <th>Producto</th>
                    <th>Empleado</th>
                    <th>Cantidad</th>
                    <th>Fecha</th>
                </tr>
            </thead>
            <tbody>
                ${movements.slice(0, 8).map((movement) => `
                    <tr>
                        <td>${escapeHtml(movement.movementType)}</td>
                        <td>${escapeHtml(movement.productName)}</td>
                        <td>${escapeHtml(movement.employeeName)}</td>
                        <td>${escapeHtml(movement.quantity)}</td>
                        <td>${formatDate(movement.createdAt)}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function renderUsers(users) {
    const panel = document.querySelector("[data-admin-panel]");
    const container = document.querySelector("[data-users-table]");
    if (!panel || !container) {
        return;
    }

    panel.hidden = false;

    container.innerHTML = `
        <table class="list-table">
            <thead>
                <tr>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Rol</th>
                    <th>Estado</th>
                </tr>
            </thead>
            <tbody>
                ${users.map((user) => `
                    <tr>
                        <td>${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)}</td>
                        <td>${escapeHtml(user.email)}</td>
                        <td>${escapeHtml(window.logiTrackAuth.formatRole(user.role))}</td>
                        <td>${user.enabled ? "Activo" : "Deshabilitado"}</td>
                    </tr>
                `).join("")}
            </tbody>
        </table>
    `;
}

function initAdminTools(auth, { users, warehouses }, me) {
    populateRoleSelect();
    populateWarehouseSelects(warehouses);
    populateManagerSelect(users, me);
    wireAdminUserForm(auth);
    wireAdminProductForm(auth);
    wireAdminWarehouseForm(auth, warehouses);
}

function renderProfile(user) {
    const container = document.querySelector("[data-profile-grid]");
    if (!container) {
        return;
    }

    const rows = [
        { label: "Nombre", value: `${user.firstName} ${user.lastName}` },
        { label: "Correo", value: user.email },
        { label: "Teléfono", value: user.phoneNumber },
        { label: "Rol", value: window.logiTrackAuth.formatRole(user.role) },
        { label: "Estado", value: user.enabled ? "Habilitado" : "Deshabilitado" },
        { label: "ID", value: user.id }
    ];

    container.innerHTML = rows.map((row) => `
        <article class="profile-card">
            <label>${escapeHtml(row.label)}</label>
            <div>${escapeHtml(row.value)}</div>
        </article>
    `).join("");
}

function wirePasswordForm(auth, notice) {
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
            const response = await auth.apiFetch("/auth/change-password", {
                method: "PATCH",
                body: JSON.stringify(payload)
            });

            showNotice(notice, response.message || "Contraseña actualizada correctamente.", "success");
            form.reset();
        } catch (error) {
            handlePageError(auth, error, notice);
        }
    });
}

function wireAdminUserForm(auth) {
    const form = document.querySelector("[data-admin-user-form]");
    const notice = document.querySelector("[data-page-notice]");
    if (!form || form.dataset.bound === "true") {
        return;
    }

    form.dataset.bound = "true";
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
            await auth.apiFetch("/users", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Usuario creado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handlePageError(auth, error, notice);
        }
    });
}

function wireAdminProductForm(auth) {
    const form = document.querySelector("[data-admin-product-form]");
    const notice = document.querySelector("[data-page-notice]");
    if (!form || form.dataset.bound === "true") {
        return;
    }

    form.dataset.bound = "true";
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const warehouseId = form.warehouseId.value;
        const payload = {
            name: form.name.value.trim(),
            category: form.category.value.trim(),
            price: Number(form.price.value),
            warehouseId: warehouseId ? Number(warehouseId) : null
        };

        try {
            await auth.apiFetch("/products", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Producto creado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handlePageError(auth, error, notice);
        }
    });
}

function wireAdminWarehouseForm(auth, warehouses) {
    const form = document.querySelector("[data-admin-warehouse-form]");
    const notice = document.querySelector("[data-page-notice]");
    if (!form || form.dataset.bound === "true") {
        return;
    }

    form.dataset.bound = "true";
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const warehouseId = Number(form.warehouseId.value);
        const managerUserId = Number(form.managerUserId.value);
        const warehouse = warehouses.find((candidate) => candidate.id === warehouseId);

        if (!warehouse) {
            showNotice(notice, "No se encontró la bodega seleccionada.", "error");
            return;
        }

        const payload = {
            name: warehouse.name,
            ubication: warehouse.ubication,
            capacity: warehouse.capacity,
            managerUserId
        };

        try {
            await auth.apiFetch(`/warehouses/${warehouseId}`, {
                method: "PUT",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Manager asignado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handlePageError(auth, error, notice);
        }
    });
}

function populateRoleSelect() {
    const select = document.querySelector('[data-admin-user-form] select[name="role"]');
    if (!select) {
        return;
    }

    select.innerHTML = `
        <option value="USER">USER</option>
        <option value="ADMIN">ADMIN</option>
    `;
}

function populateWarehouseSelects(warehouses) {
    const selects = document.querySelectorAll('select[name="warehouseId"]');
    selects.forEach((select) => {
        const allowEmpty = select.closest("[data-admin-product-form]") !== null;
        const options = warehouses.map((warehouse) => `
            <option value="${warehouse.id}">${escapeHtml(warehouse.name)}</option>
        `).join("");

        select.innerHTML = allowEmpty
            ? `<option value="">Sin asignar</option>${options}`
            : `<option value="">Selecciona una bodega</option>${options}`;
    });
}

function populateManagerSelect(users, me) {
    const select = document.querySelector('[data-admin-warehouse-form] select[name="managerUserId"]');
    if (!select) {
        return;
    }

    const candidates = users.filter((user) => user.enabled);
    select.innerHTML = `
        <option value="">Selecciona un manager</option>
        ${candidates.map((user) => `
            <option value="${user.id}" ${user.email === me.email ? "selected" : ""}>
                ${escapeHtml(user.firstName)} ${escapeHtml(user.lastName)} · ${escapeHtml(user.email)}
            </option>
        `).join("")}
    `;
}

function handlePageError(auth, error, notice) {
    if (error?.status === 401) {
        auth.logoutCurrentSession({ redirect: false });
        window.location.href = "/index.html";
        return;
    }

    showNotice(notice, error.message || "No fue posible cargar la información.", "error");
}

function showNotice(container, message, type) {
    if (!container) {
        return;
    }
    container.textContent = message;
    container.classList.add("is-visible");
    container.classList.toggle("is-success", type === "success");
    container.classList.toggle("is-error", type !== "success");
}

function formatCurrency(value) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 2
    }).format(Number(value || 0));
}

function formatDate(value) {
    if (!value) {
        return "Sin fecha";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return new Intl.DateTimeFormat("es-CO", {
        dateStyle: "medium",
        timeStyle: "short"
    }).format(date);
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
