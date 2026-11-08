export interface Product {
  productId: number | string;
  name: string;
  description?: string;
  price: number;
  category?: string;
  inventoryQuantity: number;
  /** Address of the picture, e.g. /uploads/products/ab12.jpg (use assetUrl() to show it) */
  imageUrl?: string | null;
  active?: boolean;
}
