import { apiGet } from "./Api";
import type { Product } from "../types/Product";

export const getAllProducts = (): Promise<Product[]> => apiGet<Product[]>("/product/getall");

export const getProductById = (productId: string | number): Promise<Product> =>
  apiGet<Product>(`/product/read/${encodeURIComponent(String(productId))}`);
