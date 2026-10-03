export interface Product {
  productId: number | string;
  name: string;
  description?: string;
  price: number;
  category?: string;
  inventoryQuantity: number;
}
