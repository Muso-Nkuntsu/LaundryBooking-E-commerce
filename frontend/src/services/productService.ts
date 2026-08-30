import type { Product } from "../types/Product";

const API_URL = "http://localhost:8080/product";

async function request<T>(url: string): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Request failed (${response.status})`);
  }
  return response.json();
}

export const getAllProducts = (): Promise<Product[]> =>
  request<Product[]>(`${API_URL}/getall`);

export const getProductById = (productId: string): Promise<Product> =>
  request<Product>(`${API_URL}/read/${encodeURIComponent(productId)}`);
