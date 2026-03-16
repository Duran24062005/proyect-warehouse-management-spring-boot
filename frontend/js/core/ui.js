import { resolveBackendUrl } from "./api.js";
import { logout } from "./auth.js";
import { getApiBase, isAdmin, setApiBase } from "./session.js";

let toastHost = null;
let toastSequence = 0;

function ensureToastHost() {
  if (toastHost) return toastHost;

  toastHost = document.querySelector("[data-toast-host]");
  if (!toastHost) {
    toastHost = document.createElement("div");
    toastHost.dataset.toastHost = "true";
    toastHost.className = "toast-host";
    document.body.append(toastHost);
  }

  return toastHost;
}

export function fillText(selector, value) {
  document.querySelectorAll(selector).forEach((node) => {
    node.textContent = value ?? "-";
  });
}

function getInitials(user) {
  const first = user?.firstName?.trim()?.[0] || "";
  const last = user?.lastName?.trim()?.[0] || "";
  return `${first}${last}`.toUpperCase() || "LT";
}

export function showNotice(element, message, type = "success") {
  if (!message) {
    if (element) {
      element.className = "hidden";
      element.textContent = "";
    }
    return;
  }

  const host = ensureToastHost();
  const palette = {
    success: {
      label: "Correcto",
      className: "toast-success",
      duration: 3600
    },
    error: {
      label: "Error",
      className: "toast-error",
      duration: 5200
    },
    info: {
      label: "Aviso",
      className: "toast-info",
      duration: 4200
    }
  };
  const config = palette[type] || palette.info;
  const toast = document.createElement("article");
  const toastId = `toast-${Date.now()}-${toastSequence += 1}`;

  toast.className = `toast-shell ${config.className}`;
  toast.dataset.toastId = toastId;
  toast.innerHTML = `
    <div class="toast-copy">
      <p class="toast-label">${config.label}</p>
      <p class="toast-message"></p>
    </div>
    <button class="toast-close" type="button" aria-label="Cerrar notificacion">Cerrar</button>
  `;
  toast.querySelector(".toast-message").textContent = message;

  const removeToast = () => {
    if (!toast.isConnected) return;
    toast.classList.add("toast-out");
    window.setTimeout(() => {
      toast.remove();
    }, 180);
  };

  toast.querySelector(".toast-close")?.addEventListener("click", removeToast);
  host.append(toast);

  window.setTimeout(() => {
    toast.classList.add("toast-visible");
  }, 10);
  window.setTimeout(removeToast, config.duration);
}

export function setupModal(modal) {
  if (!modal) return null;
  if (modal.dataset.modalReady === "true") {
    return {
      openModal: modal.__openModal,
      closeModal: modal.__closeModal
    };
  }

  const closeModal = () => {
    modal.classList.add("hidden");
    modal.classList.remove("flex");
    modal.setAttribute("aria-hidden", "true");
    document.body.classList.remove("modal-open");
  };

  const openModal = () => {
    modal.classList.remove("hidden");
    modal.classList.add("flex");
    modal.setAttribute("aria-hidden", "false");
    document.body.classList.add("modal-open");
  };

  modal.addEventListener("click", (event) => {
    if (event.target === modal || event.target.closest("[data-modal-close]")) {
      closeModal();
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && modal.getAttribute("aria-hidden") === "false") {
      closeModal();
    }
  });

  modal.dataset.modalReady = "true";
  modal.__openModal = openModal;
  modal.__closeModal = closeModal;

  return { openModal, closeModal };
}

export function openModal(modal) {
  if (!modal) return;

  const controller = setupModal(modal);
  controller?.openModal();
}

export function closeModal(modal) {
  if (!modal) return;

  const controller = setupModal(modal);
  controller?.closeModal();
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
  fillText(".js-user-initials", getInitials(user));

  document.querySelectorAll(".js-user-photo").forEach((image) => {
    const placeholder = image.parentElement?.querySelector(".js-user-initials");

    if (user?.profilePhotoUrl) {
      image.src = resolveBackendUrl(user.profilePhotoUrl);
      image.classList.remove("hidden");
      placeholder?.classList.add("hidden");
      return;
    }

    image.removeAttribute("src");
    image.classList.add("hidden");
    placeholder?.classList.remove("hidden");
  });

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

  if (user?.role === "EMPLOYEE") {
    document.querySelectorAll("[data-employee-hidden]").forEach((node) => {
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
