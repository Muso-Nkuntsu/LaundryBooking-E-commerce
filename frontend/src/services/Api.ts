const BASE_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api";

export class ApiError extends Error {
  status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

const buildHeaders = (extra?: HeadersInit): HeadersInit => {
  const token = localStorage.getItem("authToken");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(extra ?? {}),
  };
};

const buildQuery = (params?: Record<string, string | number | undefined>): string => {
  if (!params) return "";
  const entries = Object.entries(params).filter(([, v]) => v !== undefined) as [
    string,
    string | number
  ][];
  if (entries.length === 0) return "";
  const search = new URLSearchParams();
  entries.forEach(([key, value]) => search.set(key, String(value)));
  return `?${search.toString()}`;
};

const handleResponse = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    let message = `Request failed with status ${response.status}`;
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
    
    }
    throw new ApiError(message, response.status);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return (await response.json()) as T;
};

export const apiGet = async <T>(
  path: string,
  params?: Record<string, string | number | undefined>
): Promise<T> => {
  const response = await fetch(`${BASE_URL}${path}${buildQuery(params)}`, {
    method: "GET",
    headers: buildHeaders(),
  });
  return handleResponse<T>(response);
};

export const apiPost = async <T>(path: string, body?: unknown): Promise<T> => {
  const response = await fetch(`${BASE_URL}${path}`, {
    method: "POST",
    headers: buildHeaders(),
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  return handleResponse<T>(response);
};

export const getApiErrorMessage = (error: unknown): string => {
  if (error instanceof ApiError) {
    if (error.status === 404) {
      return "The requested resource could not be found.";
    }
    return error.message;
  }
  if (error instanceof TypeError) {
   
    return "Could not reach the server. Check your connection and try again.";
  }
  return "Something went wrong. Please try again.";
};