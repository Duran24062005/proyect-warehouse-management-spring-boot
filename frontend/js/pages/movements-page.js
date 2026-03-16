import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { fillSelect, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#movements-notice");
const form = document.querySelector("#movement-form");
const filterForm = document.querySelector("#movement-filter-form");
const resetButton = document.querySelector("#reset-movement-form");
const tableBody = document.querySelector("#movements-body");
const performedByHelp = document.querySelector("#movement-performed-by-help");
const submitButton = document.querySelector("#submit-movement-form");

let currentUser = null;
let movements = [];
let employees = [];

function syncEmployeeState() {
  const hasEmployees = employees.length > 0;

  form.performedByEmployeeId.disabled = !hasEmployees;
  submitButton.disabled = !hasEmployees;
  submitButton.classList.toggle("opacity-60", !hasEmployees);
  submitButton.classList.toggle("cursor-not-allowed", !hasEmployees);

  performedByHelp.textContent = hasEmployees
    ? "Selecciona el empleado que ejecuto fisicamente este movimiento."
    : "No hay empleados disponibles para registrar este movimiento. Primero asigna empleados a una bodega.";
}

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
  syncMovementFields();
  syncEmployeeState();
}

function editMovement(id) {
  const movement = movements.find((item) => item.id === Number(id));
  if (!movement) return;

  form.movementId.value = movement.id;
  form.movementType.value = movement.movementType;
  form.performedByEmployeeId.value = movement.performedByEmployeeId;
  form.productId.value = movement.productId;
  form.originWarehouseId.value = movement.originWarehouseId || "";
  form.destinationWarehouseId.value = movement.destinationWarehouseId || "";
  form.quantity.value = movement.quantity;
  syncMovementFields();
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadCatalogs(user) {
  const employeeRequest = user.role === "ADMIN"
    ? request("/users/role", { auth: true, query: { role: "EMPLOYEE" } })
    : request("/users/employees/my-warehouses", { auth: true });

  const [products, managedWarehouses, referenceWarehouses, employeeResponse] = await Promise.all([
    request("/products", { auth: true }),
    request("/warehouses", { auth: true }),
    request("/warehouses", {
      auth: true,
      query: { scope: "references" }
    }),
    employeeRequest
  ]);

  employees = employeeResponse;

  if (user.role !== "ADMIN" && !managedWarehouses.length) {
    showNotice(
      notice,
      "No tienes una bodega asignada como manager. Un administrador debe asignarte una para registrar y consultar movimientos.",
      "info"
    );
  }

  fillSelect(document.querySelector("#movement-product-id"), products, {
    placeholder: "Selecciona un producto",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#filter-product-id"), products, {
    placeholder: "Todos los productos",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#movement-performed-by-id"), employees, {
    placeholder: "Selecciona un empleado",
    label: (item) => `${item.firstName} ${item.lastName} - ${item.warehouseName || "Sin bodega"}`
  });
  fillSelect(document.querySelector("#movement-origin-id"), referenceWarehouses, {
    placeholder: "Sin origen",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#movement-destination-id"), referenceWarehouses, {
    placeholder: "Sin destino",
    label: (item) => item.name
  });
  fillSelect(document.querySelector("#filter-warehouse-id"), referenceWarehouses, {
    placeholder: "Todas las bodegas",
    label: (item) => item.name
  });
  document.querySelector("#movement-registered-by").textContent = `${user.firstName} ${user.lastName}`;
  syncEmployeeState();
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
        <td class="px-4 py-3">${item.performedByEmployeeName || "-"}</td>
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
    { colspan: 7, emptyMessage: "No hay movimientos para mostrar." }
  );
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  currentUser = user;
  setupLayout("movements", user);
  showNotice(notice, "");

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

  if (!form.performedByEmployeeId.value) {
    showNotice(notice, "Debes seleccionar el empleado que realizo el movimiento.", "error");
    form.performedByEmployeeId.focus();
    return;
  }

  const payload = {
    movementType: form.movementType.value,
    performedByEmployeeId: Number(form.performedByEmployeeId.value),
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
