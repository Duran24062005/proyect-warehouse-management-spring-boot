import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { createMovementAnalyticsChart } from "../core/charts.js";
import { formatDate, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#dashboard-notice");
const employeesSection = document.querySelector("#employees-summary-section");
const analyticsMeta = document.querySelector("#dashboard-analytics-meta");
const analyticsSummary = document.querySelector("#dashboard-analytics-summary");
const analyticsChart = document.querySelector("#dashboard-analytics-chart");
const analyticsLegend = document.querySelector("#dashboard-analytics-legend");

let analyticsInstance = null;

function renderAnalyticsSummary(summary = []) {
  analyticsSummary.innerHTML = summary
    .map(
      (item) => `
        <article class="chart-summary-card">
          <p class="chart-summary-label">${item.label}</p>
          <p class="chart-summary-value">${item.value}</p>
        </article>
      `
    )
    .join("");
}

function formatMovementPath(item) {
  if (item.movementType === "ENTRY") {
    return `Externo -> ${item.destinationWarehouseName || "-"}`;
  }

  if (item.movementType === "EXIT") {
    return `${item.originWarehouseName || "-"} -> Externo`;
  }

  return `${item.originWarehouseName || "-"} -> ${item.destinationWarehouseName || "-"}`;
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("system", user);
  showNotice(notice, "");

  try {
    const [products, warehouses, movements, users, employees, analytics] = await Promise.all([
      request("/products", { auth: true }),
      request("/warehouses", { auth: true }),
      request("/movements", { auth: true }),
      user.role === "ADMIN" ? request("/users", { auth: true }) : Promise.resolve([]),
      user.role === "USER" ? request("/users/employees/my-warehouses", { auth: true }) : Promise.resolve([]),
      request("/reports/analytics/movements", { auth: true, query: { window: "30d" } })
    ]);

    document.querySelector("#products-count").textContent = products.length;
    document.querySelector("#warehouses-count").textContent = warehouses.length;
    document.querySelector("#movements-count").textContent = movements.length;
    document.querySelector("#users-label").textContent = user.role === "ADMIN" ? "Usuarios" : "Empleados";
    document.querySelector("#users-count").textContent = user.role === "ADMIN" ? users.length : employees.length;

    if (user.role !== "ADMIN" && !warehouses.length) {
      showNotice(
        notice,
        "Tu usuario no tiene una bodega asignada como manager. Por eso no aparece informacion operativa en el panel.",
        "info"
      );
    }

    renderTable(
      document.querySelector("#warehouse-summary-body"),
      warehouses.slice(0, 5),
      (item) => `
        <tr>
          <td class="px-4 py-3">${item.name}</td>
          <td class="px-4 py-3">${item.ubication}</td>
          <td class="px-4 py-3">${item.managerName || "Sin manager"}</td>
        </tr>
      `,
      { colspan: 3, emptyMessage: "No hay bodegas registradas." }
    );

    renderTable(
      document.querySelector("#recent-movements-body"),
      movements.slice(0, 5),
      (item) => `
        <tr>
          <td class="px-4 py-3">${item.movementType}</td>
          <td class="px-4 py-3">${item.productName}</td>
          <td class="px-4 py-3">${formatMovementPath(item)}</td>
          <td class="px-4 py-3">${formatDate(item.createdAt)}</td>
        </tr>
      `,
      { colspan: 4, emptyMessage: "Todavia no hay movimientos registrados." }
    );

    analyticsMeta.textContent = `${analytics.subtitle}. Periodo: ${analytics.rangeLabel}.`;
    renderAnalyticsSummary(analytics.summary);
    analyticsInstance?.destroy?.();
    analyticsInstance = createMovementAnalyticsChart(analyticsChart, analytics, {
      legendContainer: analyticsLegend
    });

    if (user.role === "USER") {
      employeesSection?.classList.remove("hidden");
      renderTable(
        document.querySelector("#employees-summary-body"),
        employees,
        (item) => `
          <tr>
            <td class="px-4 py-3">
              <p class="font-medium">${item.firstName} ${item.lastName}</p>
              <p class="text-slate-500">${item.email}</p>
            </td>
            <td class="px-4 py-3">${item.phoneNumber}</td>
            <td class="px-4 py-3">${item.warehouseName || "-"}</td>
            <td class="px-4 py-3">${item.userStatus || "-"}</td>
          </tr>
        `,
        { colspan: 4, emptyMessage: "No tienes empleados asignados en tus bodegas." }
      );
    } else {
      employeesSection?.classList.add("hidden");
    }
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

init();
