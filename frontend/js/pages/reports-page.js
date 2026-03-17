import { download, request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { fillSelect, formatDate, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#reports-notice");
const form = document.querySelector("#report-form");
const typeSelect = document.querySelector("#report-type");
const previewMeta = document.querySelector("#reports-preview-meta");
const filtersChipset = document.querySelector("#reports-filters-chipset");
const summaryContainer = document.querySelector("#reports-summary");
const tableHead = document.querySelector("#reports-table-head");
const tableBody = document.querySelector("#reports-table-body");
const description = document.querySelector("#reports-description");
const managerHelp = document.querySelector("#report-manager-help");
const formatSelect = document.querySelector("#report-format");

let currentUser = null;
let catalogs = {
  products: [],
  warehouses: [],
  categories: [],
  managers: []
};

const reportCopy = {
  movements: {
    description: "Cruza producto, empleado, bodegas y cantidades para seguir el hilo operativo de entradas, salidas y transferencias."
  },
  products: {
    description: "Resume el catalogo visible para el usuario con categoria, precio y bodega asociada."
  },
  warehouses: {
    description: "Concentra la informacion de capacidad, ubicacion y manager para revisar el estado de las bodegas."
  }
};

function getCurrentType() {
  return typeSelect.value;
}

function toggleFilterGroups() {
  const type = getCurrentType();
  description.textContent = reportCopy[type].description;

  document.querySelectorAll("[data-filter-group]").forEach((group) => {
    const allowedTypes = group.dataset.filterGroup.split(" ");
    const visible = allowedTypes.includes(type);
    group.classList.toggle("hidden", !visible);
  });

  const managerGroup = document.querySelector('[data-filter-group="warehouses"]');
  const canFilterManagers = currentUser?.role === "ADMIN";
  managerGroup?.classList.toggle("hidden", type !== "warehouses" || !canFilterManagers);
  managerHelp.textContent = canFilterManagers
    ? "Puedes filtrar el reporte por manager especifico."
    : "Como manager solo veras las bodegas dentro de tu alcance actual.";
}

function buildQuery(format = "json") {
  const data = new FormData(form);
  const query = {
    type: data.get("type"),
    format
  };

  const filtersByType = {
    movements: ["productId", "warehouseId", "movementType"],
    products: ["warehouseId", "category"],
    warehouses: currentUser?.role === "ADMIN" ? ["managerUserId"] : []
  };

  filtersByType[query.type].forEach((key) => {
    const value = data.get(key);
    if (value) {
      query[key] = value;
    }
  });

  return query;
}

function renderFilters(filters = {}) {
  const entries = Object.entries(filters);
  if (!entries.length) {
    filtersChipset.innerHTML = '<span class="report-chip">Sin filtros</span>';
    return;
  }

  filtersChipset.innerHTML = entries
    .map(([key, value]) => `<span class="report-chip">${key}: ${value}</span>`)
    .join("");
}

function renderSummary(summary = []) {
  if (!summary.length) {
    summaryContainer.innerHTML = `
      <article class="report-kpi-card">
        <p class="report-kpi-label">Sin resumen</p>
        <p class="report-kpi-value">0</p>
      </article>
    `;
    return;
  }

  summaryContainer.innerHTML = summary
    .map(
      (item) => `
        <article class="report-kpi-card">
          <p class="report-kpi-label">${item.label}</p>
          <p class="report-kpi-value">${item.value}</p>
        </article>
      `
    )
    .join("");
}

function renderPreviewTable(columns = [], rows = []) {
  if (!columns.length) {
    tableHead.innerHTML = "";
    renderTable(tableBody, [], () => "", { colspan: 1, emptyMessage: "Genera una previsualizacion para ver datos aqui." });
    return;
  }

  tableHead.innerHTML = `
    <tr>
      ${columns.map((column) => `<th class="px-4 py-3">${column.label}</th>`).join("")}
    </tr>
  `;

  renderTable(
    tableBody,
    rows,
    (row) => `
      <tr>
        ${columns.map((column) => `<td class="px-4 py-3">${row[column.key] || "-"}</td>`).join("")}
      </tr>
    `,
    { colspan: columns.length, emptyMessage: "No hay registros para los filtros seleccionados." }
  );
}

function resetPreviewState() {
  previewMeta.textContent = "Todavia no has generado una vista previa.";
  renderFilters({});
  renderSummary([]);
  renderPreviewTable([], []);
}

async function loadCatalogs(user) {
  const [products, warehouses, managers] = await Promise.all([
    request("/products", { auth: true }),
    request("/warehouses", {
      auth: true,
      query: { scope: "references" }
    }),
    user.role === "ADMIN"
      ? request("/users/role", { auth: true, query: { role: "USER" } })
      : Promise.resolve([])
  ]);

  catalogs = {
    products,
    warehouses,
    managers,
    categories: [...new Set(products.map((item) => item.category).filter(Boolean))].sort((left, right) => left.localeCompare(right))
  };

  fillSelect(document.querySelector("#report-product-id"), products, {
    placeholder: "Todos los productos",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#report-warehouse-id"), warehouses, {
    placeholder: "Todas las bodegas",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#report-category"), catalogs.categories.map((category, index) => ({ id: index + 1, category })), {
    placeholder: "Todas las categorias",
    valueKey: "category",
    label: (item) => item.category
  });
  fillSelect(document.querySelector("#report-manager-user-id"), managers, {
    placeholder: user.role === "ADMIN" ? "Todos los managers" : "Sin filtro disponible",
    label: (item) => `${item.firstName} ${item.lastName}`
  });
}

async function previewReport() {
  const query = buildQuery("json");
  const preview = await request("/reports", {
    auth: true,
    query
  });

  previewMeta.textContent = `${preview.title} generado por ${preview.generatedBy} el ${formatDate(preview.generatedAt)}.`;
  renderFilters(preview.filters);
  renderSummary(preview.summary);
  renderPreviewTable(preview.columns, preview.rows);
}

async function downloadReport() {
  const query = buildQuery(formatSelect.value);
  const result = await download("/reports", {
    auth: true,
    query
  });

  const objectUrl = URL.createObjectURL(result.blob);
  const anchor = document.createElement("a");
  anchor.href = objectUrl;
  anchor.download = result.filename;
  document.body.append(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(objectUrl);
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  currentUser = user;
  setupLayout("reports", user);
  showNotice(notice, "");

  if (user.role === "EMPLOYEE") {
    showNotice(notice, "Tu rol no tiene acceso al modulo de reportes.", "error");
    form.querySelectorAll("button, select, input").forEach((element) => {
      element.disabled = true;
    });
    return;
  }

  try {
    await loadCatalogs(user);
    toggleFilterGroups();
    resetPreviewState();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

typeSelect.addEventListener("change", () => {
  toggleFilterGroups();
  resetPreviewState();
});

document.querySelector("#preview-report-button").addEventListener("click", async () => {
  try {
    await previewReport();
    showNotice(notice, "Previsualizacion actualizada.", "success");
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

document.querySelector("#download-report-button").addEventListener("click", async () => {
  try {
    await downloadReport();
    showNotice(notice, "La descarga comenzo correctamente.", "success");
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
