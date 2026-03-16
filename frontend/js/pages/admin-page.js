import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#admin-notice");
const createForm = document.querySelector("#user-create-form");
const filterForm = document.querySelector("#user-filter-form");
const tableBody = document.querySelector("#users-body");

let filters = {
  role: "",
  status: ""
};

function renderQuickActions(user) {
  const actions = [];

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

  if (!actions.length) {
    return `<span class="text-sm text-slate-500">Sin acciones</span>`;
  }

  return actions.join("");
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

  const users = await request(path, {
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
    { colspan: 5, emptyMessage: "No hay usuarios para mostrar." }
  );
}

async function init() {
  const user = await requireAuth({ adminOnly: true });
  if (!user) return;

  setupLayout("admin", user);

  try {
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

createForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const data = Object.fromEntries(new FormData(createForm).entries());
  data.enabled = data.enabled === "true";

  try {
    await request("/users", {
      method: "POST",
      body: data,
      auth: true
    });
    showNotice(notice, "Usuario creado correctamente.", "success");
    createForm.reset();
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
  const form = event.target.closest("[data-status-form]");
  if (!form) return;

  event.preventDefault();

  try {
    await request(`/users/${form.dataset.statusForm}/status`, {
      method: "PATCH",
      body: { status: form.status.value },
      auth: true
    });
    showNotice(notice, "Estado actualizado correctamente.", "success");
    await loadUsers();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

tableBody.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-mode]");
  if (!button) return;

  try {
    await request(`/users/${button.dataset.id}/${button.dataset.mode}`, {
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
