import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import {
  closeModal,
  fillSelect,
  formatMoney,
  openModal,
  renderTable,
  setupModal,
  setupLayout,
  showNotice
} from "../core/ui.js";

const notice = document.querySelector("#products-notice");
const form = document.querySelector("#product-form");
const modal = document.querySelector("#product-modal");
const openModalButton = document.querySelector("#open-product-modal");
const resetButton = document.querySelector("#reset-product-form");
const formTitle = document.querySelector("#product-form-title");
const submitButton = document.querySelector("#submit-product-form");
const tableBody = document.querySelector("#products-body");
const warehouseSelect = document.querySelector("#product-warehouse-id");
const warehouseHelp = document.querySelector("#product-warehouse-help");

let products = [];
let warehouses = [];

function resetForm() {
  form.reset();
  form.productId.value = "";
  formTitle.textContent = "Nuevo producto";
  submitButton.textContent = "Guardar producto";
  warehouseSelect.disabled = false;
  warehouseHelp.textContent = "Define la bodega actual del activo al momento de crearlo.";
}

function editProduct(id) {
  const product = products.find((item) => item.id === Number(id));
  if (!product) return;

  form.productId.value = product.id;
  form.name.value = product.name;
  form.category.value = product.category;
  form.price.value = product.price;
  form.warehouseId.value = product.warehouseId || "";
  formTitle.textContent = "Editar producto";
  submitButton.textContent = "Guardar cambios";
  warehouseSelect.disabled = true;
  warehouseHelp.textContent = "La bodega actual de un activo individual se cambia desde el modulo de movimientos.";
  openModal(modal);
}

async function loadData(user) {
  const [productsResponse, warehousesResponse] = await Promise.all([
    request("/products", { auth: true }),
    request("/warehouses", { auth: true })
  ]);

  products = productsResponse;
  warehouses = warehousesResponse;

  fillSelect(warehouseSelect, warehouses, {
    placeholder: "Sin bodega",
    label: (item) => item.name
  });

  if (user.role !== "ADMIN" && !warehouses.length) {
    showNotice(
      notice,
      "No tienes una bodega asignada como manager. Por eso no hay productos visibles para tu usuario.",
      "info"
    );
  }

  renderTable(
    tableBody,
    products,
    (item) => `
      <tr>
        <td class="px-4 py-3">${item.name}</td>
        <td class="px-4 py-3">${item.category}</td>
        <td class="px-4 py-3">${formatMoney(item.price)}</td>
        <td class="px-4 py-3">${item.warehouseName || "-"}</td>
        <td class="px-4 py-3">
          <div class="flex flex-wrap gap-2">
            <button class="btn-secondary" data-edit-id="${item.id}" type="button">Editar</button>
            <button class="btn-danger" data-delete-id="${item.id}" type="button">Eliminar</button>
          </div>
        </td>
      </tr>
    `,
    { colspan: 5, emptyMessage: "No hay productos creados." }
  );
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("products", user);
  setupModal(modal);
  showNotice(notice, "");

  try {
    await loadData(user);
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

openModalButton?.addEventListener("click", () => {
  resetForm();
  openModal(modal);
});

resetButton.addEventListener("click", resetForm);

tableBody.addEventListener("click", async (event) => {
  const editButton = event.target.closest("[data-edit-id]");
  const deleteButton = event.target.closest("[data-delete-id]");

  if (editButton) {
    editProduct(editButton.dataset.editId);
  }

  if (deleteButton) {
    const confirmed = window.confirm("Se eliminara este producto. Deseas continuar?");
    if (!confirmed) return;

    try {
      await request(`/products/${deleteButton.dataset.deleteId}`, {
        method: "DELETE",
        auth: true
      });
      showNotice(notice, "Producto eliminado correctamente.", "success");
      await loadData(await requireAuth());
      resetForm();
    } catch (error) {
      showNotice(notice, error.message, "error");
    }
  }
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = {
    name: form.name.value.trim(),
    category: form.category.value.trim(),
    price: Number(form.price.value),
    warehouseId: form.warehouseId.value ? Number(form.warehouseId.value) : null
  };

  const productId = form.productId.value;

  try {
    await request(productId ? `/products/${productId}` : "/products", {
      method: productId ? "PUT" : "POST",
      body: payload,
      auth: true
    });

    showNotice(
      notice,
      productId ? "Producto actualizado correctamente." : "Producto creado correctamente.",
      "success"
    );
    resetForm();
    closeModal(modal);
    await loadData(await requireAuth());
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
