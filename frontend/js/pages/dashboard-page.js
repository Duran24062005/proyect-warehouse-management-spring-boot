import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { formatDate, renderTable, setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#dashboard-notice");

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("system", user);

  try {
    const [products, warehouses, movements, lowStock, users] = await Promise.all([
      request("/products", { auth: true }),
      request("/warehouses", { auth: true }),
      request("/movements", { auth: true }),
      request("/products/low-stock", { auth: true }),
      user.role === "ADMIN" ? request("/users", { auth: true }) : Promise.resolve([])
    ]);

    document.querySelector("#products-count").textContent = products.length;
    document.querySelector("#warehouses-count").textContent = warehouses.length;
    document.querySelector("#movements-count").textContent = movements.length;
    document.querySelector("#users-count").textContent = user.role === "ADMIN" ? users.length : "No aplica";

    renderTable(
      document.querySelector("#low-stock-body"),
      lowStock,
      (item) => `
        <tr>
          <td class="px-4 py-3">${item.name}</td>
          <td class="px-4 py-3">${item.category}</td>
          <td class="px-4 py-3">${item.warehouseName || "-"}</td>
        </tr>
      `,
      { colspan: 3, emptyMessage: "No hay productos con stock bajo." }
    );

    renderTable(
      document.querySelector("#recent-movements-body"),
      movements.slice(0, 5),
      (item) => `
        <tr>
          <td class="px-4 py-3">${item.movementType}</td>
          <td class="px-4 py-3">${item.productName}</td>
          <td class="px-4 py-3">${item.quantity}</td>
          <td class="px-4 py-3">${formatDate(item.createdAt)}</td>
        </tr>
      `,
      { colspan: 4, emptyMessage: "Todavia no hay movimientos registrados." }
    );
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
}

init();
