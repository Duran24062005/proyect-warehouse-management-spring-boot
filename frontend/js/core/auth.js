import { request } from "./api.js";
import {
  clearSession,
  getUser,
  getToken,
  isAuthenticated,
  setUser
} from "./session.js";

function inPlatform() {
  return window.location.pathname.includes("/platform/");
}

function isProfilePage() {
  return window.location.pathname.endsWith("/profile.html");
}

export function getLoginPath() {
  return inPlatform() ? "../index.html" : "./index.html";
}

export function getRegisterPath() {
  return inPlatform() ? "../register.html" : "./register.html";
}

export function getDashboardPath() {
  const user = getUser();
  const target = user?.role === "EMPLOYEE" ? "profile.html" : "system.html";
  return inPlatform() ? `./${target}` : `./platform/${target}`;
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

    if (user.role === "EMPLOYEE" && !isProfilePage()) {
      window.location.href = inPlatform() ? "./profile.html" : "./platform/profile.html";
      return null;
    }

    return user;
  } catch (_error) {
    logout();
    return null;
  }
}
