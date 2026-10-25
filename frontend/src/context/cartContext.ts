import { createContext } from "react";

export interface CartItem {
  productId: number;
  name: string;
  price: number;
  quantity: number;
  /** Stock when the item was added, so the cart can't go above it. The backend checks again at checkout. */
  stock: number;
}

export interface CartApi {
  items: CartItem[];
  /** Number of units in the cart, for the badge on the cart icon. */
  count: number;
  total: number;
  /** Adds up to `quantity` units without going over the stock. Returns how many were actually added. */
  add: (product: { productId: number | string; name: string; price: number; inventoryQuantity: number }, quantity?: number) => number;
  setQuantity: (productId: number, quantity: number) => void;
  remove: (productId: number) => void;
  clear: () => void;
  quantityOf: (productId: number | string) => number;
}

export const CartContext = createContext<CartApi | null>(null);
