const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const DEFAULT_TIMEOUT_MS = 10000;
const USE_MOCKS = import.meta.env.VITE_USE_MOCKS === 'true';

export class ApiError extends Error {
  constructor(message, { status, cause } = {}) {
    super(message);
    this.name = 'ApiError';
    this.status = status ?? null;
    this.cause = cause ?? null;
  }
}

function getAuthToken() {
  
  return localStorage.getItem('authToken');
}

async function request(path, { method = 'GET', body, signal, timeoutMs = DEFAULT_TIMEOUT_MS } = {}) {
  if (USE_MOCKS) {
    const { mockRequest } = await import('./mockAdapter');
    return mockRequest(path, method);
  }

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  
  if (signal) {
    signal.addEventListener('abort', () => controller.abort(), { once: true });
  }

  const token = getAuthToken();

  try {
    const response = await fetch(`${BASE_URL}${path}`, {
      method,
      headers: {
        Accept: 'application/json',
        ...(body ? { 'Content-Type': 'application/json' } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });

    let payload = null;
    const text = await response.text();
    if (text) {
      try {
        payload = JSON.parse(text);
      } catch {
        payload = text;
      }
    }

    if (!response.ok) {
      const message =
        (payload && typeof payload === 'object' && (payload.message || payload.error)) ||
        `Request failed with status ${response.status}`;
      throw new ApiError(message, { status: response.status });
    }

    return payload;
  } catch (err) {
    if (err instanceof ApiError) throw err;
    if (err.name === 'AbortError') {
      throw new ApiError('The request timed out. Check your connection and try again.', {
        cause: err,
      });
    }
    throw new ApiError('Something went wrong reaching the server. Please try again.', {
      cause: err,
    });
  } finally {
    clearTimeout(timeout);
  }
}

export const apiClient = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  patch: (path, body, options) => request(path, { ...options, method: 'PATCH', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
};