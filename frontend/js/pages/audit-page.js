import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import {
  fillSelect,
  formatDate,
  renderTable,
  setupLayout,
  showNotice
} from "../core/ui.js";

const notice = document.querySelector("#audit-notice");
const filterForm = document.querySelector("#audit-filter-form");
const resetButton = document.querySelector("#audit-reset-button");
const tableBody = document.querySelector("#audit-body");
const summaryContainer = document.querySelector("#audit-summary");
const filterChipset = document.querySelector("#audit-filter-chipset");
const meta = document.querySelector("#audit-meta");
const scopePill = document.querySelector("#audit-scope-pill");
const actorSelect = document.querySelector("#audit-actor-user-id");
const warehouseSelect = document.querySelector("#audit-warehouse-id");
const detailMeta = document.querySelector("#audit-detail-meta");
const detailBadges = document.querySelector("#audit-detail-badges");
const oldValuesContainer = document.querySelector("#audit-old-values");
const newValuesContainer = document.querySelector("#audit-new-values");

let currentUser = null;
let auditRows = [];
let visibleWarehouses = [];
let selectedAuditId = null;

const entityLabels = {
  app_user: "Usuarios",
  product: "Productos",
  warehouse: "Bodegas",
  movement: "Movimientos"
};

const operationLabels = {
  INSERT: "Creacion",
  UPDATE: "Actualizacion",
  DELETE: "Eliminacion"
};

function buildQuery() {
  const data = new FormData(filterForm);
  return Object.fromEntries(
    Array.from(data.entries()).filter(([, value]) => value !== "")
  );
}

function getWarehouseSelectScope(user) {
  return user.role === "ADMIN"
    ? request("/warehouses", { auth: true, query: { scope: "references" } })
    : request("/warehouses", { auth: true });
}

function getActorLabel(item) {
  if (item.actorUserName) {
    return item.actorUserEmail
      ? `${item.actorUserName} (${item.actorUserEmail})`
      : item.actorUserName;
  }

  return item.actorUserEmail || "Sistema";
}

function renderFilters(query) {
  const labels = {
    operationType: "Operacion",
    entityName: "Entidad",
    actorUserId: "Actor",
    warehouseId: "Bodega"
  };

  const humanized = Object.entries(query).map(([key, value]) => {
    if (key === "entityName") {
      return `${labels[key]}: ${entityLabels[value] || value}`;
    }

    if (key === "operationType") {
      return `${labels[key]}: ${operationLabels[value] || value}`;
    }

    if (key === "actorUserId") {
      const actor = auditRows.find((item) => String(item.actorUserId || "") === value);
      return `${labels[key]}: ${actor ? getActorLabel(actor) : value}`;
    }

    if (key === "warehouseId") {
      const warehouse = visibleWarehouses.find((item) => String(item.id) === value);
      return `${labels[key]}: ${warehouse?.name || value}`;
    }

    return `${labels[key] || key}: ${value}`;
  });

  filterChipset.innerHTML = humanized.length
    ? humanized.map((item) => `<span class="report-chip">${item}</span>`).join("")
    : '<span class="report-chip">Sin filtros</span>';
}

function renderSummary(items) {
  const counts = {
    total: items.length,
    insert: items.filter((item) => item.operationType === "INSERT").length,
    update: items.filter((item) => item.operationType === "UPDATE").length,
    delete: items.filter((item) => item.operationType === "DELETE").length
  };

  summaryContainer.innerHTML = [
    { label: "Eventos visibles", value: counts.total },
    { label: "Creaciones", value: counts.insert },
    { label: "Actualizaciones", value: counts.update },
    { label: "Eliminaciones", value: counts.delete }
  ]
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

function isLocalDateTimeArray(value) {
  return Array.isArray(value)
    && (value.length === 6 || value.length === 7)
    && value.slice(0, 6).every((item) => Number.isInteger(item));
}

function formatLocalDateTimeArray(value) {
  if (!isLocalDateTimeArray(value)) {
    return null;
  }

  const [year, month, day, hour, minute, second, nano = 0] = value;
  const milliseconds = Math.floor(Number(nano || 0) / 1000000);
  const date = new Date(year, month - 1, day, hour, minute, second, milliseconds);

  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "long",
    timeStyle: "medium"
  }).format(date);
}

function prettifyFieldKey(key) {
  if (!key) return "-";

  const normalized = String(key)
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/[_-]+/g, " ")
    .trim();

  return normalized.charAt(0).toUpperCase() + normalized.slice(1);
}

function formatFieldValue(value) {
  if (value === null || value === undefined) {
    return "null";
  }

  const localDateTime = formatLocalDateTimeArray(value);
  if (localDateTime) {
    return localDateTime;
  }

  if (typeof value === "string") {
    return value;
  }

  if (typeof value === "number" || typeof value === "boolean") {
    return String(value);
  }

  if (Array.isArray(value)) {
    return value
      .map((item) => formatFieldValue(item))
      .join(", ");
  }

  return JSON.stringify(value, null, 2);
}

function collectChangedFields(item) {
  const oldValues = item.oldValues || {};
  const newValues = item.newValues || {};
  const fields = new Set([...Object.keys(oldValues), ...Object.keys(newValues)]);

  return Array.from(fields).filter((field) => JSON.stringify(oldValues[field]) !== JSON.stringify(newValues[field]));
}

function buildRowSummary(item) {
  if (item.operationType === "INSERT") {
    return "Registro creado con snapshot inicial.";
  }

  if (item.operationType === "DELETE") {
    return "Registro eliminado con snapshot previo.";
  }

  const changedFields = collectChangedFields(item);
  if (!changedFields.length) {
    return "Cambio registrado sin diferencias visibles.";
  }

  return `Campos: ${changedFields.slice(0, 4).join(", ")}${changedFields.length > 4 ? "..." : ""}`;
}

function renderJsonPayload(container, payload, operationType, side) {
  if (!payload || !Object.keys(payload).length) {
    const emptyMessages = {
      INSERT: {
        old: "En una insercion no existen valores anteriores.",
        new: "Sin snapshot nuevo disponible."
      },
      UPDATE: {
        old: "Sin snapshot anterior disponible.",
        new: "Sin snapshot nuevo disponible."
      },
      DELETE: {
        old: "Sin snapshot anterior disponible.",
        new: "En una eliminacion no existen valores nuevos."
      }
    };

    container.innerHTML = `<p class="text-sm text-slate-400">${emptyMessages[operationType]?.[side] || "Sin snapshot disponible."}</p>`;
    return;
  }

  container.innerHTML = Object.entries(payload)
    .map(
      ([key, value]) => `
        <article class="audit-field-row">
          <p class="audit-field-key">${prettifyFieldKey(key)}</p>
          <pre class="audit-field-value">${formatFieldValue(value)}</pre>
        </article>
      `
    )
    .join("");
}

function renderSelectedDetail(item) {
  if (!item) {
    detailMeta.textContent = "Selecciona un evento para ver sus snapshots completos.";
    detailBadges.innerHTML = "";
    oldValuesContainer.innerHTML = '<p class="text-sm text-slate-400">Sin evento seleccionado.</p>';
    newValuesContainer.innerHTML = '<p class="text-sm text-slate-400">Sin evento seleccionado.</p>';
    return;
  }

  detailMeta.textContent = `${getActorLabel(item)} · ${formatDate(item.createdAt)} · ${item.entityDescription || "Sin descripcion"}`;
  detailBadges.innerHTML = `
    <span class="audit-detail-badge">${operationLabels[item.operationType] || item.operationType}</span>
    <span class="audit-detail-badge">${entityLabels[item.entityName] || item.entityName}</span>
    <span class="audit-detail-badge">ID ${item.id}</span>
  `;
  renderJsonPayload(oldValuesContainer, item.oldValues, item.operationType, "old");
  renderJsonPayload(newValuesContainer, item.newValues, item.operationType, "new");
}

function updateActorOptions(items) {
  const currentValue = actorSelect.value;
  const actors = Array.from(
    new Map(
      items
        .filter((item) => item.actorUserId)
        .map((item) => [
          item.actorUserId,
          {
            id: item.actorUserId,
            label: getActorLabel(item)
          }
        ])
    ).values()
  ).sort((left, right) => left.label.localeCompare(right.label));

  fillSelect(actorSelect, actors, {
    placeholder: "Todos los actores",
    label: (item) => item.label
  });

  if (actors.some((item) => String(item.id) === currentValue)) {
    actorSelect.value = currentValue;
  }
}

function renderTableRows(items) {
  renderTable(
    tableBody,
    items,
    (item) => `
      <tr class="${item.id === selectedAuditId ? "audit-row-active" : ""}">
        <td class="px-4 py-3">${formatDate(item.createdAt)}</td>
        <td class="px-4 py-3">
          <p class="font-medium">${item.actorUserName || "Sistema"}</p>
          <p class="text-slate-500">${item.actorUserEmail || "-"}</p>
        </td>
        <td class="px-4 py-3">${operationLabels[item.operationType] || item.operationType}</td>
        <td class="px-4 py-3">
          <p class="font-medium">${entityLabels[item.entityName] || item.entityName}</p>
          <p class="text-slate-500">${item.entityDescription || "-"}</p>
        </td>
        <td class="px-4 py-3">${buildRowSummary(item)}</td>
        <td class="px-4 py-3">
          <button class="btn-secondary" data-detail-id="${item.id}" type="button">Ver cambios</button>
        </td>
      </tr>
    `,
    { colspan: 6, emptyMessage: "No hay eventos de auditoria para los filtros seleccionados." }
  );
}

async function loadCatalogs(user) {
  visibleWarehouses = await getWarehouseSelectScope(user);
  fillSelect(warehouseSelect, visibleWarehouses, {
    placeholder: "Todas las bodegas visibles",
    label: (item) => item.name
  });

  scopePill.textContent = user.role === "ADMIN"
    ? "Historial global"
    : "Solo tu alcance de bodega";
}

async function loadAuditHistory() {
  const query = buildQuery();
  const items = await request("/audit-changes", {
    auth: true,
    query
  });

  auditRows = items;
  selectedAuditId = items[0]?.id || null;
  meta.textContent = `${items.length} eventos visibles para ${currentUser.role === "ADMIN" ? "todo el sistema" : "tu alcance actual"}.`;
  renderFilters(query);
  renderSummary(items);
  updateActorOptions(items);
  renderTableRows(items);
  renderSelectedDetail(items[0] || null);
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  currentUser = user;
  setupLayout("audit", user);
  showNotice(notice, "");

  try {
    await loadCatalogs(user);
    await loadAuditHistory();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

filterForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  try {
    await loadAuditHistory();
    showNotice(notice, "Historial actualizado correctamente.", "success");
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

resetButton.addEventListener("click", async () => {
  filterForm.reset();

  try {
    await loadAuditHistory();
    showNotice(notice, "Filtros reiniciados correctamente.", "success");
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

tableBody.addEventListener("click", (event) => {
  const button = event.target.closest("[data-detail-id]");
  if (!button) return;

  const item = auditRows.find((row) => row.id === Number(button.dataset.detailId));
  if (!item) return;

  selectedAuditId = item.id;
  renderTableRows(auditRows);
  renderSelectedDetail(item);
});

init();
