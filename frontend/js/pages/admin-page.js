import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import {
  closeModal,
  fillSelect,
  openModal,
  renderTable,
  setupLayout,
  setupModal,
  showNotice
} from "../core/ui.js";

const notice = document.querySelector("#admin-notice");
const form = document.querySelector("#user-create-form");
const modal = document.querySelector("#user-modal");
const filterForm = document.querySelector("#user-filter-form");
const tableBody = document.querySelector("#users-body");
const openModalButton = document.querySelector("#open-user-modal");
const resetButton = document.querySelector("#reset-user-form");
const formTitle = document.querySelector("#user-form-title");
const submitButton = document.querySelector("#user-submit-button");
const warehouseShell = document.querySelector("#user-warehouse-shell");
const warehouseSelect = form.querySelector("select[name='warehouseId']");
const passwordShell = document.querySelector("#user-password-shell");
const enabledShell = document.querySelector("#user-enabled-shell");
const emailInput = form.querySelector("input[name='email']");
const passwordInput = form.querySelector("input[name='password']");

let filters = {
  role: "",
  status: ""
};

let users = [];
let warehouses = [];

function syncRoleFields() {
  const isEmployee = form.role.value === "EMPLOYEE";
  warehouseShell.classList.toggle("hidden", !isEmployee);
  warehouseSelect.required = isEmployee;

  if (!isEmployee) {
    warehouseSelect.value = "";
  }
}

function setCreateMode() {
  form.reset();
  form.userId.value = "";
  formTitle.textContent = "Crear usuario";
  submitButton.textContent = "Crear usuario";
  emailInput.readOnly = false;
  passwordShell.classList.remove("hidden");
  enabledShell.classList.remove("hidden");
  passwordInput.required = true;
  form.role.value = "USER";
  syncRoleFields();
}

function editUser(id) {
  const user = users.find((item) => item.id === Number(id));
  if (!user) return;

  form.userId.value = user.id;
  formTitle.textContent = "Editar usuario";
  submitButton.textContent = "Guardar cambios";
  emailInput.value = user.email;
  emailInput.readOnly = true;
  passwordShell.classList.add("hidden");
  enabledShell.classList.add("hidden");
  passwordInput.required = false;
  passwordInput.value = "";
  form.firstName.value = user.firstName;
  form.lastName.value = user.lastName;
  form.phoneNumber.value = user.phoneNumber;
  form.role.value = user.role;
  form.warehouseId.value = user.warehouseId || "";
  syncRoleFields();
  openModal(modal);
}

function renderQuickActions(user) {
  const actions = [
    `<button class="btn-secondary" data-edit-id="${user.id}" type="button">Editar</button>`
  ];

  if (user.userStatus === "PENDING") {
    actions.push(
      `<button class="btn-secondary" data-mode="approve" data-id="${user.id}" type="button">Aprobar</button>`
    );
    actions.push(
      `<button class="btn-danger" data-mode="block" data-id="${user.id}" type="button">Bloquear</button>`
    );
  }

  if (user.userStatus === "ACTIVE") {
    actions.push(
      `<button class="btn-danger" data-mode="block" data-id="${user.id}" type="button">Bloquear</button>`
    );
  }

  if (user.userStatus === "BLOCKED") {
    actions.push(
      `<button class="btn-secondary" data-mode="unblock" data-id="${user.id}" type="button">Desbloquear</button>`
    );
  }

  return actions.join("");
}

async function loadCatalogs() {
  warehouses = await request("/warehouses", {
    auth: true,
    query: { scope: "references" }
  });

  fillSelect(warehouseSelect, warehouses, {
    placeholder: "Selecciona una bodega",
    label: (item) => item.name
  });
}

async function loadUsers() {
  let path = "/users";
  let query = {};

  if (filters.role) {
    path = "/users/role";
    query = { role: filters.role };
  } else if (filters.status) {
    path = "/users/status";
    query = { status: filters.status };
  }

  users = await request(path, {
    auth: true,
    query
  });

  renderTable(
    tableBody,
    users,
    (item) => `
      <tr>
        <td class="px-4 py-3">
          <p class="font-medium">${item.firstName} ${item.lastName}</p>
          <p class="text-slate-500">${item.email}</p>
        </td>
        <td class="px-4 py-3">${item.role}</td>
        <td class="px-4 py-3">${item.warehouseName || "-"}</td>
        <td class="px-4 py-3">${item.userStatus || "-"}</td>
        <td class="px-4 py-3">
          <form class="compact-actions flex flex-wrap gap-2 lg:items-center" data-status-form="${item.id}">
            <select class="select-shell compact-control max-w-[140px] lg:max-w-[124px]" name="status">
              <option value="PENDING" ${item.userStatus === "PENDING" ? "selected" : ""}>PENDING</option>
              <option value="ACTIVE" ${item.userStatus === "ACTIVE" ? "selected" : ""}>ACTIVE</option>
              <option value="BLOCKED" ${item.userStatus === "BLOCKED" ? "selected" : ""}>BLOCKED</option>
            </select>
            <button class="btn-secondary" type="submit">Guardar</button>
          </form>
        </td>
        <td class="px-4 py-3">
          <div class="compact-actions flex flex-wrap gap-2">
            ${renderQuickActions(item)}
          </div>
        </td>
      </tr>
    `,
    { colspan: 6, emptyMessage: "No hay usuarios para mostrar." }
  );
}

async function init() {
  const user = await requireAuth({ adminOnly: true });
  if (!user) return;

  setupLayout("admin", user);
  setupModal(modal);
  setCreateMode();

  try {
    await loadCatalogs();
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

form.role.addEventListener("change", syncRoleFields);
openModalButton?.addEventListener("click", () => {
  setCreateMode();
  openModal(modal);
});
resetButton.addEventListener("click", setCreateMode);

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const isEditing = Boolean(form.userId.value);
  const warehouseId = form.warehouseId.value ? Number(form.warehouseId.value) : null;

  try {
    if (isEditing) {
      await request(`/users/${form.userId.value}`, {
        method: "PUT",
        body: {
          firstName: form.firstName.value.trim(),
          lastName: form.lastName.value.trim(),
          phoneNumber: form.phoneNumber.value.trim(),
          role: form.role.value,
          warehouseId
        },
        auth: true
      });
      showNotice(notice, "Usuario actualizado correctamente.", "success");
    } else {
      await request("/users", {
        method: "POST",
        body: {
          email: form.email.value.trim(),
          password: form.password.value,
          firstName: form.firstName.value.trim(),
          lastName: form.lastName.value.trim(),
          phoneNumber: form.phoneNumber.value.trim(),
          role: form.role.value,
          warehouseId,
          enabled: form.enabled.value === "true"
        },
        auth: true
      });
      showNotice(notice, "Usuario creado correctamente.", "success");
    }

    setCreateMode();
    closeModal(modal);
    await loadCatalogs();
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

filterForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  filters = Object.fromEntries(new FormData(filterForm).entries());

  try {
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

tableBody.addEventListener("submit", async (event) => {
  const statusForm = event.target.closest("[data-status-form]");
  if (!statusForm) return;

  event.preventDefault();

  try {
    await request(`/users/${statusForm.dataset.statusForm}/status`, {
      method: "PATCH",
      body: { status: statusForm.status.value },
      auth: true
    });
    showNotice(notice, "Estado actualizado correctamente.", "success");
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

tableBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest("[data-edit-id]");
  const actionButton = event.target.closest("[data-mode]");

  if (editButton) {
    editUser(editButton.dataset.editId);
    return;
  }

  if (!actionButton) return;

  try {
    await request(`/users/${actionButton.dataset.id}/${actionButton.dataset.mode}`, {
      method: "PATCH",
      auth: true
    });
    showNotice(notice, "Accion aplicada correctamente.", "success");
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
