import { apiFetch } from "../core/auth.js";
import { escapeHtml, formatCurrency, renderTable, showNotice } from "../core/ui.js";
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
    setActiveNavigation("products");
    wireGlobalAppActions();

    try {
        const [me, products, lowStock, warehouses] = await Promise.all([
            apiFetch("/auth/me"),
            apiFetch("/products"),
            apiFetch("/products/low-stock"),
            apiFetch("/warehouses")
        ]);

        renderSidebarSession(me);
        renderSessionSwitcher();
        guardAdminView(me);
        renderHero(products, lowStock, warehouses);
        renderProducts(products);
        renderLowStock(lowStock);
        populateWarehouseSelect(warehouses);

        if (me.role === "ADMIN") {
            wireCreateProductForm(notice);
        }
    } catch (error) {
        handleAppError(error, notice);
    }
});

function renderHero(products, lowStock, warehouses) {
    document.querySelector("[data-page-heading]").textContent = "Catálogo y operación de productos";
    document.querySelector("[data-page-lead]").textContent = "Aquí puedes revisar el catálogo completo, detectar stock bajo y, si eres administrador, crear nuevos productos con asignación inicial de bodega.";

    const stats = [
        { label: "Productos", value: products.length },
        { label: "Con stock bajo", value: lowStock.length },
        { label: "Bodegas vinculables", value: warehouses.length }
    ];

    document.querySelector("[data-stats-grid]").innerHTML = stats.map((item) => `
        <article>
            <span class="stat-label">${escapeHtml(item.label)}</span>
            <strong class="stat-value">${escapeHtml(item.value)}</strong>
        </article>
    `).join("");
}

function renderProducts(products) {
    document.querySelector("[data-products-table]").innerHTML = renderTable(
        [
            { label: "Producto", render: (row) => escapeHtml(row.name) },
            { label: "Categoría", render: (row) => escapeHtml(row.category) },
            { label: "Precio", render: (row) => formatCurrency(row.price) },
            { label: "Bodega", render: (row) => escapeHtml(row.warehouseName || "Sin asignar") }
        ],
        products
    );
}

function renderLowStock(lowStock) {
    const container = document.querySelector("[data-low-stock-list]");
    if (!lowStock.length) {
        container.innerHTML = '<p class="empty-state">No hay productos con stock bajo.</p>';
        return;
    }

    container.innerHTML = lowStock.map((product) => `
        <article class="list-item">
            <div>
                <strong>${escapeHtml(product.name)}</strong>
                <div class="list-meta">${escapeHtml(product.category)} · ${escapeHtml(product.warehouseName || "Sin bodega")}</div>
            </div>
            <span class="badge badge--soft">Stock bajo</span>
        </article>
    `).join("");
}

function populateWarehouseSelect(warehouses) {
    const select = document.querySelector('[data-create-product-form] select[name="warehouseId"]');
    if (!select) {
        return;
    }

    select.innerHTML = `
        <option value="">Sin asignar</option>
        ${warehouses.map((warehouse) => `
            <option value="${warehouse.id}">${escapeHtml(warehouse.name)}</option>
        `).join("")}
    `;
}

function wireCreateProductForm(notice) {
    const form = document.querySelector("[data-create-product-form]");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const payload = {
            name: form.name.value.trim(),
            category: form.category.value.trim(),
            price: Number(form.price.value),
            warehouseId: form.warehouseId.value ? Number(form.warehouseId.value) : null
        };

        try {
            await apiFetch("/products", {
                method: "POST",
                body: JSON.stringify(payload)
            });

            showNotice(notice, "Producto creado correctamente.", "success");
            window.location.reload();
        } catch (error) {
            handleAppError(error, notice);
        }
    });
}
