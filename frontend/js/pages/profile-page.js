import { request } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#profile-notice");
const form = document.querySelector("#profile-form");

function renderProfile(user) {
  document.querySelector("#profile-name").textContent = `${user.firstName} ${user.lastName}`;
  document.querySelector("#profile-email").textContent = user.email;
  document.querySelector("#profile-phone").textContent = user.phoneNumber;
  document.querySelector("#profile-role").textContent = user.role;
  document.querySelector("#profile-status").textContent = user.userStatus || "-";
}

async function init() {
  const user = await requireAuth();
  if (!user) return;

  setupLayout("profile", user);
  renderProfile(user);
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const payload = Object.fromEntries(new FormData(form).entries());

  try {
    const response = await request("/auth/change-password", {
      method: "PATCH",
      body: payload,
      auth: true
    });
    showNotice(notice, response.message || "Contrasena actualizada.", "success");
    form.reset();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
