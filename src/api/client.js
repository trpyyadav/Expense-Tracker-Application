const TOKEN_KEY = 'et_token';
const USER_KEY = 'et_user';

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (t) => localStorage.setItem(TOKEN_KEY, t),
  getUser: () => {
    try {
      return JSON.parse(localStorage.getItem(USER_KEY));
    } catch {
      return null;
    }
  },
  setUser: (u) => localStorage.setItem(USER_KEY, JSON.stringify(u)),
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

let onUnauthorized = () => {};
export const setUnauthorizedHandler = (fn) => {
  onUnauthorized = fn;
};

export class ApiError extends Error {
  constructor(message, status, fields) {
    super(message);
    this.status = status;
    this.fields = fields || null; // field -> message map for validation errors
  }
}

// Backend error shapes:
//   validation (400): { fieldName: "message", ... }
//   everything else:  { error: "message" }
async function parseError(res) {
  let body = null;
  try {
    body = await res.json();
  } catch {
    /* empty body */
  }
  if (body && typeof body === 'object') {
    if (typeof body.error === 'string') return new ApiError(body.error, res.status);
    const values = Object.values(body).filter((v) => typeof v === 'string');
    if (values.length) return new ApiError(values.join('. '), res.status, body);
  }
  const fallback = {
    401: 'Your session has expired. Please sign in again.',
    403: 'Your session has expired. Please sign in again.',
    404: 'Not found.',
    500: 'Server error. Please try again.',
  };
  return new ApiError(fallback[res.status] || `Request failed (${res.status})`, res.status);
}

async function request(path, { method = 'GET', params, body, blob = false } = {}) {
  const url = new URL(`/api${path}`, window.location.origin);
  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
    });
  }

  const headers = {};
  const token = tokenStore.get();
  if (token) headers.Authorization = `Bearer ${token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let res;
  try {
    res = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(
      'Cannot reach the server. Make sure the Spring Boot backend is running on port 8081.',
      0
    );
  }

  if (!res.ok) {
    const isAuthCall = path.startsWith('/auth/');
    // Spring Security answers with 401/403 when the JWT is missing or expired.
    if ((res.status === 401 || res.status === 403) && !isAuthCall && token) {
      onUnauthorized();
    }
    throw await parseError(res);
  }

  if (res.status === 204) return null;
  if (blob) return res.blob();
  return res.json();
}

export const http = {
  get: (path, params) => request(path, { params }),
  post: (path, body) => request(path, { method: 'POST', body }),
  put: (path, body) => request(path, { method: 'PUT', body }),
  del: (path) => request(path, { method: 'DELETE' }),
  blob: (path, params) => request(path, { params, blob: true }),
};
