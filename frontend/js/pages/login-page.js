import { request } from "../core/api.js";
import { getDashboardPath, redirectIfAuthenticated } from "../core/auth.js";
import { saveLogin } from "../core/session.js";
import { setupLayout, showNotice } from "../core/ui.js";

const notice = document.querySelector("#login-notice");
const form = document.querySelector("#login-form");

redirectIfAuthenticated();
setupLayout("login", {});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  showNotice(notice, "");

  const data = Object.fromEntries(new FormData(form).entries());

  try {
    const response = await request("/auth/login", {
      method: "POST",
      body: data
    });

    saveLogin(response);
    window.location.href = getDashboardPath();
  } catch (error) {
    showNotice(notice, error.message, "error");
  }
});
