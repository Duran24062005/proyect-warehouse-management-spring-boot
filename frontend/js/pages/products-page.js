import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import {
  fillSelect,
  formatMoney,
  renderTable,
  setupLayout,
  showNotice
} from "../core/ui.js";

const notice = document.querySelector("#products-notice");
const form = document.querySelector("#product-form");
const resetButton = document.querySelector("#reset-product-form");
const tableBody = document.querySelector("#products-body");
const lowTableBody = document.querySelector("#low-products-body");
const warehouseSelect = document.querySelector("#product-warehouse-id");

let products = [];
let warehouses = [];

function resetForm() {
  form.reset();
  form.productId.value = "";
}

function editProduct(id) {
  const product = products.find((item) => item.id === Number(id));
  if (!product) return;

  form.productId.value = product.id;
  form.name.value = product.name;
  form.category.value = product.category;
  form.price.value = product.price;
  form.warehouseId.value = product.warehouseId || "";
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function loadData() {
  const [productsResponse, warehousesResponse, lowStockResponse] = await Promise.all([
    request("/products", { auth: true }),
    request("/warehouses", { auth: true }),
    request("/products/low-stock", { auth: true })
  ]);

  products = productsResponse;
  warehouses = warehousesResponse;

  fillSelect(warehouseSelect, warehouses, {
    placeholder: "Sin bodega",
    label: (item) => item.name
  });

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

  renderTable(
    lowTableBody,
    lowStockResponse,
    (item) => `
      <tr>
        <td class="px-4 py-3">${item.name}</td>
        <td class="px-4 py-3">${item.category}</td>
        <td class="px-4 py-3">${item.warehouseName || "-"}</td>
      </tr>
    `,
    { colspan: 3, emptyMessage: "No hay alertas de stock bajo." }
  );
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("products", user);

  try {
    await loadData();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

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
      await loadData();
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
    await loadData();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
