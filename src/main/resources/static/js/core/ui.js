export function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

export function escapeAttribute(value) {
    return escapeHtml(value);
}

export function showNotice(container, message, type = "success") {
    if (!container) {
        return;
    }

    container.textContent = message;
    container.classList.add("is-visible");
    container.classList.toggle("is-success", type === "success");
    container.classList.toggle("is-error", type !== "success");
}

export function clearNotice(container) {
    if (!container) {
        return;
    }

    container.textContent = "";
    container.classList.remove("is-visible", "is-success", "is-error");
}

export function formatCurrency(value) {
    return new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 2
    }).format(Number(value || 0));
}

export function formatDate(value) {
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

export function renderTable(columns, rows) {
    if (!rows.length) {
        return '<p class="empty-state">No hay datos disponibles.</p>';
    }

    return `
        <div class="table-wrap">
            <table class="data-table">
                <thead>
                    <tr>${columns.map((column) => `<th>${escapeHtml(column.label)}</th>`).join("")}</tr>
                </thead>
                <tbody>
                    ${rows.map((row) => `
                        <tr>${columns.map((column) => `<td>${column.render(row)}</td>`).join("")}</tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
    `;
}
