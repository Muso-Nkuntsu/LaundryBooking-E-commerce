import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import { CartContext } from "./cartContext";
import type { CartApi, CartItem } from "./cartContext";

// The cart lives in the browser until checkout, so it survives a page refresh.
// It only becomes an order on the backend when the student checks out.
const KEY = "laundry.cart";

function loadCart(): CartItem[] {
  try {
    const raw = localStorage.getItem(KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? (parsed as CartItem[]).filter((item) => typeof item?.productId === "number" && item.quantity > 0) : [];
  } catch {
    return [];
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(loadCart);

  useEffect(() => {
    try {
      localStorage.setItem(KEY, JSON.stringify(items));
    } catch {
      // Storage can be unavailable in private windows; the cart then lasts until the page is closed.
    }
  }, [items]);

  const add = useCallback<CartApi["add"]>((product, quantity = 1) => {
    const productId = Number(product.productId);
    const stock = Math.max(product.inventoryQuantity, 0);
    const existing = items.find((item) => item.productId === productId);
    const inCart = existing?.quantity ?? 0;
    const added = Math.max(Math.min(quantity, stock - inCart), 0);
    if (added === 0) return 0;

    setItems((prev) =>
      existing
        ? prev.map((item) => (item.productId === productId ? { ...item, quantity: item.quantity + added, stock, price: product.price } : item))
        : [...prev, { productId, name: product.name, price: product.price, quantity: added, stock }],
    );
    return added;
  }, [items]);

  const setQuantity = useCallback((productId: number, quantity: number) => {
    setItems((prev) =>
      prev
        .map((item) => (item.productId === productId ? { ...item, quantity: Math.min(Math.max(quantity, 0), item.stock) } : item))
        .filter((item) => item.quantity > 0),
    );
  }, []);

  const remove = useCallback((productId: number) => {
    setItems((prev) => prev.filter((item) => item.productId !== productId));
  }, []);

  const clear = useCallback(() => setItems([]), []);

  const api = useMemo<CartApi>(
    () => ({
      items,
      count: items.reduce((sum, item) => sum + item.quantity, 0),
      total: Math.round(items.reduce((sum, item) => sum + item.price * item.quantity, 0) * 100) / 100,
      add,
      setQuantity,
      remove,
      clear,
      quantityOf: (productId) => items.find((item) => item.productId === Number(productId))?.quantity ?? 0,
    }),
    [items, add, setQuantity, remove, clear],
  );

  return <CartContext.Provider value={api}>{children}</CartContext.Provider>;
}
