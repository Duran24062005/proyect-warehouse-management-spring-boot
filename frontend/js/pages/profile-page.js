import { request, resolveBackendUrl } from "../core/api.js";
import { requireAuth } from "../core/auth.js";
import { setUser } from "../core/session.js";
import { setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#profile-notice");
const form = document.querySelector("#profile-form");
const photoForm = document.querySelector("#profile-photo-form");
const photoInput = document.querySelector("#profile-photo-input");
const photoImage = document.querySelector("#profile-photo-image");
const photoPlaceholder = document.querySelector("#profile-photo-placeholder");

function getInitials(user) {
  const first = user.firstName?.trim()?.[0] || "";
  const last = user.lastName?.trim()?.[0] || "";
  return `${first}${last}`.toUpperCase() || "LT";
}

function renderProfile(user) {
  document.querySelector("#profile-name").textContent = `${user.firstName} ${user.lastName}`;
  document.querySelector("#profile-email").textContent = user.email;
  document.querySelector("#profile-phone").textContent = user.phoneNumber;
  document.querySelector("#profile-role").textContent = user.role;
  document.querySelector("#profile-status").textContent = user.userStatus || "-";
  document.querySelector("#profile-warehouse").textContent = user.warehouseName || "Sin bodega asignada";

  if (user.profilePhotoUrl) {
    photoImage.src = resolveBackendUrl(user.profilePhotoUrl);
    photoImage.classList.remove("hidden");
    photoPlaceholder.classList.add("hidden");
  } else {
    photoImage.removeAttribute("src");
    photoImage.classList.add("hidden");
    photoPlaceholder.textContent = getInitials(user);
    photoPlaceholder.classList.remove("hidden");
  }
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

photoForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!photoInput.files?.length) {
    showNotice(notice, "Debes seleccionar una imagen.", "error");
    return;
  }

  const payload = new FormData();
  payload.append("file", photoInput.files[0]);

  try {
    const response = await request("/users/me/profile-photo", {
      method: "PATCH",
      body: payload,
      auth: true
    });
    setUser(response);
    renderProfile(response);
    photoForm.reset();
    showNotice(notice, "Foto de perfil actualizada correctamente.", "success");
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});

init();
