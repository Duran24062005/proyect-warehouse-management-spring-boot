import { clearSession, getApiBase, getToken } from "./session.js";

function buildUrl(path, query) {
  const url = new URL(`${getApiBase()}${path}`);

  Object.entries(query || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      url.searchParams.set(key, value);
    }
  });

  return url.toString();
}

async function parseResponse(response) {
  const contentType = response.headers.get("content-type") || "";

  if (contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}

export async function request(path, options = {}) {
  const { method = "GET", body, query, auth = false } = options;
  const headers = {
    Accept: "application/json"
  };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (auth) {
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined
  });

  const data = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      clearSession();
    }

    const message =
      data?.message ||
      data?.error ||
      (typeof data === "string" && data) ||
      "No se pudo completar la solicitud";

    throw new Error(message);
  }

  return data;
}
