import type { OrderStatus } from "./order";

// Shape of an order item as returned by the backend (/order-item/...).
export interface OrderItem {
  orderItemId: number;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  order?: {
    orderId?: number;
    orderDate?: string;
    totalAmount?: number;
    status?: OrderStatus;
  };
  product?: {
    productId?: number | string;
    name?: string;
    productName?: string;
  };
}
