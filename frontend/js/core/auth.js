import { request } from "./api.js";
import {
  clearSession,
  getToken,
  isAuthenticated,
  setUser
} from "./session.js";

function inPlatform() {
  return window.location.pathname.includes("/platform/");
}

export function getLoginPath() {
  return inPlatform() ? "../index.html" : "./index.html";
}

export function getRegisterPath() {
  return inPlatform() ? "../register.html" : "./register.html";
}

export function getDashboardPath() {
  return inPlatform() ? "./system.html" : "./platform/system.html";
}

export function logout() {
  clearSession();
  window.location.href = getLoginPath();
}

export function redirectIfAuthenticated() {
  if (isAuthenticated()) {
    window.location.href = getDashboardPath();
  }
}

export async function requireAuth(options = {}) {
  const { adminOnly = false } = options;

  if (!getToken()) {
    window.location.href = getLoginPath();
    return null;
  }

  try {
    const user = await request("/auth/me", { auth: true });
    setUser(user);

    if (adminOnly && user.role !== "ADMIN") {
      window.location.href = inPlatform() ? "./system.html" : getDashboardPath();
      return null;
    }

    return user;
  } catch (_error) {
    logout();
    return null;
  }
}
