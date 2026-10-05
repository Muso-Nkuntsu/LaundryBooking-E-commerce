// One place for every call to the backend.
// Set VITE_API_BASE_URL in a .env file to point at a different server.
const BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

type QueryParams = Record<string, string | number | undefined | null>;

const buildQuery = (params?: QueryParams): string => {
  if (!params) return "";
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null) search.set(key, String(value));
  });
  const text = search.toString();
  return text ? `?${text}` : "";
};

// The backend sends errors as plain text; Spring's default errors are JSON with a "message".
const readErrorMessage = async (response: Response): Promise<string> => {
  const fallback = `Request failed with status ${response.status}`;
  try {
    const text = (await response.text()).trim();
    if (!text) return fallback;
    if (text.startsWith("{")) {
      const body = JSON.parse(text) as { message?: string; error?: string };
      return body.message || body.error || fallback;
    }
    return text;
  } catch {
    return fallback;
  }
};

const handleResponse = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    throw new ApiError(await readErrorMessage(response), response.status);
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
};

interface RequestOptions {
  params?: QueryParams;
  body?: unknown;
}

const request = async <T>(method: string, path: string, { params, body }: RequestOptions = {}): Promise<T> => {
  const response = await fetch(`${BASE_URL}${path}${buildQuery(params)}`, {
    method,
    headers: body !== undefined ? { "Content-Type": "application/json" } : undefined,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  return handleResponse<T>(response);
};

export const apiGet = <T>(path: string, params?: QueryParams): Promise<T> => request<T>("GET", path, { params });

export const apiPost = <T>(path: string, body?: unknown, params?: QueryParams): Promise<T> =>
  request<T>("POST", path, { body, params });

export const apiPut = <T>(path: string, body?: unknown, params?: QueryParams): Promise<T> =>
  request<T>("PUT", path, { body, params });

export const apiDelete = <T = void>(path: string): Promise<T> => request<T>("DELETE", path);

export const getApiErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    if (error.status === 404 && error.message.startsWith("Request failed")) {
      return "The requested resource could not be found.";
    }
    return error.message;
  }
  if (error instanceof TypeError) {
    return "Can't reach the server. Check that the backend is running, then try again.";
  }
  return "Something went wrong. Please try again.";
};
