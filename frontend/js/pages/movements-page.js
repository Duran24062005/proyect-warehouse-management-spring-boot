import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { fillSelect, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#movements-notice");
const form = document.querySelector("#movement-form");
const filterForm = document.querySelector("#movement-filter-form");
const resetButton = document.querySelector("#reset-movement-form");
const tableBody = document.querySelector("#movements-body");

let currentUser = null;
let movements = [];
let users = [];

function syncMovementFields() {
  const type = document.querySelector("#movement-type").value;
  const originShell = document.querySelector("#origin-shell");
  const destinationShell = document.querySelector("#destination-shell");
  const originSelect = document.querySelector("#movement-origin-id");
  const destinationSelect = document.querySelector("#movement-destination-id");

  originShell.classList.toggle("hidden", type === "ENTRY");
  destinationShell.classList.toggle("hidden", type === "EXIT");

  if (type === "ENTRY") {
    originSelect.value = "";
  }

  if (type === "EXIT") {
    destinationSelect.value = "";
  }
}

function resetForm() {
  form.reset();
  form.movementId.value = "";
  if (currentUser) {
    form.employeeUserId.value = currentUser.id;
  }
  syncMovementFields();
}

function editMovement(id) {
  const movement = movements.find((item) => item.id === Number(id));
  if (!movement) return;

  form.movementId.value = movement.id;
  form.movementType.value = movement.movementType;
  form.employeeUserId.value = movement.employeeUserId;
  form.productId.value = movement.productId;
  form.originWarehouseId.value = movement.originWarehouseId || "";
  form.destinationWarehouseId.value = movement.destinationWarehouseId || "";
  form.quantity.value = movement.quantity;
  syncMovementFields();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadCatalogs(user) {
  const [products, warehouses, usersResponse] = await Promise.all([
    request("/products", { auth: true }),
    request("/warehouses", { auth: true }),
    user.role === "ADMIN" ? request("/users", { auth: true }) : Promise.resolve([user])
  ]);

  users = usersResponse;

  fillSelect(document.querySelector("#movement-product-id"), products, {
    placeholder: "Selecciona un producto",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#filter-product-id"), products, {
    placeholder: "Todos los productos",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#movement-origin-id"), warehouses, {
    placeholder: "Sin origen",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#movement-destination-id"), warehouses, {
    placeholder: "Sin destino",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#filter-warehouse-id"), warehouses, {
    placeholder: "Todas las bodegas",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#movement-employee-id"), users, {
    placeholder: "Selecciona un empleado",
    label: (item) => `${item.firstName} ${item.lastName}`
  });

  form.employeeUserId.value = user.id;
}

async function loadMovements(filters = {}) {
  movements = await request("/movements", {
    auth: true,
    query: filters
  });

  renderTable(
    tableBody,
    movements,
    (item) => `
      <tr>
        <td class="px-4 py-3">${item.movementType}</td>
        <td class="px-4 py-3">${item.productName}</td>
        <td class="px-4 py-3">${item.originWarehouseName || "-"}</td>
        <td class="px-4 py-3">${item.destinationWarehouseName || "-"}</td>
        <td class="px-4 py-3">${item.quantity}</td>
        <td class="px-4 py-3">
          <div class="flex flex-wrap gap-2">
            <button class="btn-secondary" data-edit-id="${item.id}" type="button">Editar</button>
            <button class="btn-danger" data-delete-id="${item.id}" type="button">Eliminar</button>
          </div>
        </td>
      </tr>
    `,
    { colspan: 6, emptyMessage: "No hay movimientos para mostrar." }
  );
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  currentUser = user;
  setupLayout("movements", user);

  try {
    await loadCatalogs(user);
    await loadMovements();
    syncMovementFields();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

document.querySelector("#movement-type").addEventListener("change", syncMovementFields);
resetButton.addEventListener("click", resetForm);

filterForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const filters = Object.fromEntries(new FormData(filterForm).entries());

  try {
    await loadMovements(filters);
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

tableBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest("[data-edit-id]");
  const deleteButton = event.target.closest("[data-delete-id]");

  if (editButton) {
    editMovement(editButton.dataset.editId);
  }

  if (deleteButton) {
    const confirmed = window.confirm("Se eliminara este movimiento. Deseas continuar?");
    if (!confirmed) return;

    try {
      await request(`/movements/${deleteButton.dataset.deleteId}`, {
        method: "DELETE",
        auth: true
      });
      showNotice(notice, "Movimiento eliminado correctamente.", "success");
      resetForm();
      await loadMovements();
    } catch (error) {
      showNotice(notice, error.message, "error");
    }
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    movementType: form.movementType.value,
    employeeUserId: Number(form.employeeUserId.value),
    originWarehouseId: form.originWarehouseId.value ? Number(form.originWarehouseId.value) : null,
    destinationWarehouseId: form.destinationWarehouseId.value ? Number(form.destinationWarehouseId.value) : null,
    productId: Number(form.productId.value),
    quantity: Number(form.quantity.value)
  };

  const movementId = form.movementId.value;

  try {
    await request(movementId ? `/movements/${movementId}` : "/movements", {
      method: movementId ? "PUT" : "POST",
      body: payload,
      auth: true
    });
    showNotice(
      notice,
      movementId ? "Movimiento actualizado correctamente." : "Movimiento creado correctamente.",
      "success"
    );
    resetForm();
    await loadMovements();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
