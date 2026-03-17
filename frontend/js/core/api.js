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

function buildHeaders({ auth = false, body } = {}) {
  const isFormData = body instanceof FormData;
  const headers = {
    Accept: "application/json"
  };

  if (body !== undefined && !isFormData) {
    headers["Content-Type"] = "application/json";
  }

  if (auth) {
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }

  return { headers, isFormData };
}

export function getBackendBaseUrl() {
  const url = new URL(getApiBase());
  url.pathname = url.pathname.replace(/\/api\/?$/, "/");
  return url.toString().replace(/\/$/, "");
}

export function resolveBackendUrl(path) {
  if (!path) return "";
  if (/^https?:\/\//i.test(path)) return path;
  return `${getBackendBaseUrl()}${path.startsWith("/") ? path : `/${path}`}`;
}

export async function request(path, options = {}) {
  const { method = "GET", body, query, auth = false } = options;
  const { headers, isFormData } = buildHeaders({ auth, body });
  const url = buildUrl(path, query);

  const response = await fetch(url, {
    method,
    headers,
    body:
      body === undefined
        ? undefined
        : isFormData
          ? body
          : JSON.stringify(body)
  });

  const data = await parseResponse(response);

  if (!response.ok) {
    if (response.status === 401) {
      clearSession();
    }

    const details = Array.isArray(data?.details) ? data.details.filter(Boolean) : [];
    const message =
      data?.message ||
      data?.error ||
      (typeof data === "string" && data) ||
      "No se pudo completar la solicitud";

    console.error("API request failed", {
      method,
      url,
      status: response.status,
      statusText: response.statusText,
      requestBody: body,
      responseBody: data,
      details
    });

    const fullMessage = details.length ? `${message}: ${details.join(" | ")}` : message;
    const error = new Error(fullMessage);
    error.status = response.status;
    error.payload = data;
    throw error;
  }

  return data;
}

function extractFilename(response, fallback = "download.bin") {
  const contentDisposition = response.headers.get("content-disposition") || "";
  const match = contentDisposition.match(/filename=\"?([^\";]+)\"?/i);
  return match?.[1] || fallback;
}

export async function download(path, options = {}) {
  const { method = "GET", body, query, auth = false, fallbackFilename } = options;
  const { headers, isFormData } = buildHeaders({ auth, body });
  headers.Accept = "*/*";
  const url = buildUrl(path, query);

  const response = await fetch(url, {
    method,
    headers,
    body:
      body === undefined
        ? undefined
        : isFormData
          ? body
          : JSON.stringify(body)
  });

  if (!response.ok) {
    const data = await parseResponse(response);

    if (response.status === 401) {
      clearSession();
    }

    const details = Array.isArray(data?.details) ? data.details.filter(Boolean) : [];
    const message =
      data?.message ||
      data?.error ||
      (typeof data === "string" && data) ||
      "No se pudo completar la solicitud";

    const fullMessage = details.length ? `${message}: ${details.join(" | ")}` : message;
    const error = new Error(fullMessage);
    error.status = response.status;
    error.payload = data;
    throw error;
  }

  const blob = await response.blob();
  return {
    blob,
    filename: extractFilename(response, fallbackFilename || "download.bin"),
    contentType: response.headers.get("content-type") || blob.type
  };
}
