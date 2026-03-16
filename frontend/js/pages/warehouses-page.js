import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { fillSelect, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#warehouses-notice");
const form = document.querySelector("#warehouse-form");
const formCard = document.querySelector("#warehouse-form-card");
const resetButton = document.querySelector("#reset-warehouse-form");
const managerSelect = document.querySelector("#warehouse-manager-id");
const tableBody = document.querySelector("#warehouses-body");

let warehouses = [];

function resetForm() {
  form.reset();
  form.warehouseId.value = "";
}

function editWarehouse(id) {
  const warehouse = warehouses.find((item) => item.id === Number(id));
  if (!warehouse) return;

  form.warehouseId.value = warehouse.id;
  form.name.value = warehouse.name;
  form.ubication.value = warehouse.ubication;
  form.capacity.value = warehouse.capacity || "";
  form.managerUserId.value = warehouse.managerUserId || "";
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadData(user) {
  const requests = [
    request("/warehouses", { auth: true }),
    user.role === "ADMIN" ? request("/users", { auth: true }) : Promise.resolve([])
  ];

  const [warehousesResponse, usersResponse] = await Promise.all(requests);
  warehouses = warehousesResponse;

  if (user.role !== "ADMIN") {
    formCard?.classList.add("hidden");

    if (!warehouses.length) {
      showNotice(
        notice,
        "No tienes una bodega asignada como manager. Un administrador debe asignarte una para ver informacion operativa.",
        "info"
      );
    }
  } else {
    formCard?.classList.remove("hidden");
  }

  fillSelect(managerSelect, usersResponse.filter((item) => item.userStatus === "ACTIVE" && item.role === "USER"), {
    placeholder: "Sin manager asignado",
    label: (item) => `${item.firstName} ${item.lastName}`
  });

  renderTable(
    tableBody,
    warehouses,
    (item) => `
      <tr>
        <td class="px-4 py-3">${item.name}</td>
        <td class="px-4 py-3">${item.ubication}</td>
        <td class="px-4 py-3">${item.capacity ?? "-"}</td>
        <td class="px-4 py-3">${item.managerName || "-"}</td>
        <td class="px-4 py-3">
          <div class="flex flex-wrap gap-2">
            <button class="btn-secondary" data-edit-id="${item.id}" type="button">Editar</button>
            <button class="btn-danger" data-delete-id="${item.id}" type="button">Eliminar</button>
          </div>
        </td>
      </tr>
    `,
    { colspan: 5, emptyMessage: "No hay bodegas creadas." }
  );
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("warehouses", user);
  showNotice(notice, "");

  try {
    await loadData(user);
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

resetButton.addEventListener("click", resetForm);

tableBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest("[data-edit-id]");
  const deleteButton = event.target.closest("[data-delete-id]");

  if (editButton) {
    editWarehouse(editButton.dataset.editId);
  }

  if (deleteButton) {
    const confirmed = window.confirm("Se eliminara esta bodega. Deseas continuar?");
    if (!confirmed) return;

    try {
      await request(`/warehouses/${deleteButton.dataset.deleteId}`, {
        method: "DELETE",
        auth: true
      });
      showNotice(notice, "Bodega eliminada correctamente.", "success");
      resetForm();
      await init();
    } catch (error) {
      showNotice(notice, error.message, "error");
    }
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    name: form.name.value.trim(),
    ubication: form.ubication.value.trim(),
    capacity: form.capacity.value ? Number(form.capacity.value) : null,
    managerUserId: form.managerUserId.value ? Number(form.managerUserId.value) : null
  };

  const warehouseId = form.warehouseId.value;

  try {
    await request(warehouseId ? `/warehouses/${warehouseId}` : "/warehouses", {
      method: warehouseId ? "PUT" : "POST",
      body: payload,
      auth: true
    });
    showNotice(
      notice,
      warehouseId ? "Bodega actualizada correctamente." : "Bodega creada correctamente.",
      "success"
    );
    resetForm();
    await init();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
