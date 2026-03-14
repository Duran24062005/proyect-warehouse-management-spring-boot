const API_BASE_KEY = "logitrack-api-base";
const TOKEN_KEY = "logitrack-token";
const USER_KEY = "logitrack-user";

export function getApiBase() {
  return localStorage.getItem(API_BASE_KEY) || "http://localhost:8000/api";
}

export function setApiBase(value) {
  const normalized = String(value || "").trim().replace(/\/+$/, "");
  localStorage.setItem(API_BASE_KEY, normalized || "http://localhost:8000/api");
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function getUser() {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;

  try {
    return JSON.parse(raw);
  } catch (_error) {
    return null;
  }
}

export function setUser(user) {
  if (!user) {
    localStorage.removeItem(USER_KEY);
    return;
  }

  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function saveLogin(response) {
  localStorage.setItem(TOKEN_KEY, response.token || "");
  setUser(response.user || null);
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
}

export function isAuthenticated() {
  return Boolean(getToken());
}

export function isAdmin() {
  return getUser()?.role === "ADMIN";
}
