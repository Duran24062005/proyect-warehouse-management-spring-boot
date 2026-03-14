import { logout } from "./auth.js";
import { getApiBase, isAdmin, setApiBase } from "./session.js";

export function fillText(selector, value) {
  document.querySelectorAll(selector).forEach((node) => {
    node.textContent = value ?? "-";
  });
}

export function showNotice(element, message, type = "success") {
  if (!element) return;

  if (!message) {
    element.className = "hidden";
    element.textContent = "";
    return;
  }

  const palette = {
    success: "border-moss/30 bg-moss/10 text-moss",
    error: "border-wine/30 bg-wine/10 text-wine",
    info: "border-ink/20 bg-white/80 text-ink"
  };

  element.className = `rounded-2xl border px-4 py-3 text-sm ${palette[type] || palette.info}`;
  element.textContent = message;
}

export function formatDate(value) {
  if (!value) return "-";

  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

export function formatMoney(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("es-CO", {
    style: "currency",
    currency: "COP",
    maximumFractionDigits: 0
  }).format(amount);
}

export function fillSelect(select, items, options = {}) {
  if (!select) return;

  const {
    placeholder = "Selecciona una opcion",
    valueKey = "id",
    label = (item) => item.name ?? item.email ?? item.id,
    includeEmpty = true
  } = options;

  const choices = [];
  if (includeEmpty) {
    choices.push(`<option value="">${placeholder}</option>`);
  }

  items.forEach((item) => {
    choices.push(`<option value="${item[valueKey]}">${label(item)}</option>`);
  });

  select.innerHTML = choices.join("");
}

export function renderTable(tbody, items, renderRow, options = {}) {
  if (!tbody) return;

  const { colspan = 1, emptyMessage = "No hay datos para mostrar." } = options;

  if (!items.length) {
    tbody.innerHTML = `<tr><td colspan="${colspan}" class="px-4 py-6 text-center text-sm text-slate-500">${emptyMessage}</td></tr>`;
    return;
  }

  tbody.innerHTML = items.map(renderRow).join("");
}

export function setupLayout(pageKey, user) {
  const userName =
    user?.firstName && user?.lastName ? `${user.firstName} ${user.lastName}` : "Invitado";
  const userRole = user?.role || "";

  fillText(".js-user-name", userName);
  fillText(".js-user-role", userRole);
  fillText(".js-api-base", getApiBase());

  document.querySelectorAll("[data-nav]").forEach((link) => {
    if (link.dataset.nav === pageKey) {
      link.classList.add("nav-link-active");
    }
  });

  if (!isAdmin()) {
    document.querySelectorAll("[data-admin-only]").forEach((node) => {
      node.classList.add("hidden");
    });
  }

  document.querySelectorAll("[data-action='logout']").forEach((button) => {
    button.addEventListener("click", logout);
  });

  document.querySelectorAll("[data-api-base-form]").forEach((form) => {
    const input = form.querySelector("input[name='apiBase']");
    const feedback = form.querySelector("[data-api-base-feedback]");

    if (input) {
      input.value = getApiBase();
    }

    form.addEventListener("submit", (event) => {
      event.preventDefault();
      setApiBase(input?.value || "");
      fillText(".js-api-base", getApiBase());
      showNotice(feedback, "Base URL guardada correctamente.", "success");
    });
  });
}
