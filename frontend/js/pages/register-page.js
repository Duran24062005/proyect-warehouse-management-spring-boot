import { request } from "../core/api.js";
import { getLoginPath, redirectIfAuthenticated } from "../core/auth.js";
import { setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#register-notice");
const form = document.querySelector("#register-form");

redirectIfAuthenticated();
setupLayout("register", {});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  showNotice(notice, "");

  const data = Object.fromEntries(new FormData(form).entries());

  try {
    await request("/auth/register", {
      method: "POST",
      body: data
    });

    showNotice(notice, "Registro completado. Ahora puedes iniciar sesion.", "success");
    form.reset();
    window.setTimeout(() => {
      window.location.href = getLoginPath();
    }, 1200);
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});
