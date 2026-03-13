import { apiFetch } from "../core/auth.js";
import { escapeHtml, formatCurrency, formatDate, renderTable } from "../core/ui.js";
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
    setActiveNavigation("dashboard");
    wireGlobalAppActions();

    try {
        const [me, products, lowStock, warehouses, movements] = await Promise.all([
            apiFetch("/auth/me"),
            apiFetch("/products"),
            apiFetch("/products/low-stock"),
            apiFetch("/warehouses"),
            apiFetch("/movements")
        ]);

        renderSidebarSession(me);
        renderSessionSwitcher();
        guardAdminView(me);
        renderHero(me, { products, lowStock, warehouses, movements });
        renderProductsPreview(products);
        renderLowStock(lowStock);
        renderWarehouses(warehouses);
        renderMovements(movements);
    } catch (error) {
        handleAppError(error, notice);
    }
});

function renderHero(me, data) {
    document.querySelector("[data-page-heading]").textContent = `${me.firstName}, así se ve tu operación hoy`;
    document.querySelector("[data-page-lead]").textContent = "Este panel resume la salud operativa del sistema y te da una lectura rápida de productos, stock, bodegas y movimientos recientes.";

    const stats = [
        { label: "Productos", value: data.products.length },
        { label: "Stock bajo", value: data.lowStock.length },
        { label: "Almacenes", value: data.warehouses.length },
        { label: "Movimientos", value: data.movements.length }
    ];

    document.querySelector("[data-stats-grid]").innerHTML = stats.map((item) => `
        <article>
            <span class="stat-label">${escapeHtml(item.label)}</span>
            <strong class="stat-value">${escapeHtml(item.value)}</strong>
        </article>
    `).join("");
}

function renderProductsPreview(products) {
    const container = document.querySelector("[data-products-preview]");
    container.innerHTML = renderTable(
        [
            { label: "Producto", render: (row) => escapeHtml(row.name) },
            { label: "Categoría", render: (row) => escapeHtml(row.category) },
            { label: "Precio", render: (row) => formatCurrency(row.price) },
            { label: "Bodega", render: (row) => escapeHtml(row.warehouseName || "Sin asignar") }
        ],
        products.slice(0, 6)
    );
}

function renderMovements(movements) {
    const container = document.querySelector("[data-movements-preview]");
    container.innerHTML = renderTable(
        [
            { label: "Tipo", render: (row) => escapeHtml(row.movementType) },
            { label: "Producto", render: (row) => escapeHtml(row.productName) },
            { label: "Empleado", render: (row) => escapeHtml(row.employeeName) },
            { label: "Fecha", render: (row) => formatDate(row.createdAt) }
        ],
        movements.slice(0, 6)
    );
}

function renderLowStock(products) {
    const container = document.querySelector("[data-low-stock-list]");
    if (!products.length) {
        container.innerHTML = '<p class="empty-state">No hay alertas de stock bajo.</p>';
        return;
    }

    container.innerHTML = products.map((product) => `
        <article class="list-item">
            <div>
                <strong>${escapeHtml(product.name)}</strong>
                <div class="list-meta">${escapeHtml(product.category)} · ${escapeHtml(product.warehouseName || "Sin bodega")}</div>
            </div>
            <span class="badge badge--soft">Atención</span>
        </article>
    `).join("");
}

function renderWarehouses(warehouses) {
    const container = document.querySelector("[data-warehouse-list]");
    if (!warehouses.length) {
        container.innerHTML = '<p class="empty-state">No hay almacenes registrados.</p>';
        return;
    }

    container.innerHTML = warehouses.slice(0, 5).map((warehouse) => `
        <article class="list-item">
            <div>
                <strong>${escapeHtml(warehouse.name)}</strong>
                <div class="list-meta">${escapeHtml(warehouse.ubication)} · Manager: ${escapeHtml(warehouse.managerName || "Sin asignar")}</div>
            </div>
            <span class="badge">${escapeHtml(warehouse.capacity ?? "0")} cap.</span>
        </article>
    `).join("");
}
