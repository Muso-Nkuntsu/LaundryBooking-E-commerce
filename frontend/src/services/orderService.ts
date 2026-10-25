import { apiGet, apiPatch, apiPost } from "./Api";
import type { Order } from "../types/order";

export interface CheckoutItem {
  productId: number;
  quantity: number;
}

export const orderService = {
  /**
   * Turns the cart into an order. Only product IDs and quantities are sent:
   * the backend looks up the prices, checks the stock and works out the total.
   * The order starts as PENDING and becomes PAID once it is paid for.
   */
  checkout(studentId: number, items: CheckoutItem[]): Promise<Order> {
    return apiPost<Order>("/api/orders/checkout", { studentId, items });
  },

  getOrdersByStudent(studentId: number): Promise<Order[]> {
    return apiGet<Order[]>(`/api/orders/student/${studentId}`);
  },

  /** Cancels an unpaid order; the backend puts the stock back. */
  cancelOrder(orderId: number): Promise<Order> {
    return apiPatch<Order>(`/api/orders/${orderId}/cancel`);
  },
};
