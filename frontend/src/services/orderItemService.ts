import { apiDelete, apiGet } from "./Api";
import type { OrderItem } from "../types/orderItem";

export const orderItemService = {
  /** Everything one student has ordered. */
  getOrderItemsByStudent(studentId: number): Promise<OrderItem[]> {
    return apiGet<OrderItem[]>(`/order-item/student/${studentId}`);
  },

  /** Every order item in the system (for admin screens). */
  getAllOrderItems(): Promise<OrderItem[]> {
    return apiGet<OrderItem[]>("/order-item/getall");
  },

  getOrderItemById(id: number): Promise<OrderItem> {
    return apiGet<OrderItem>(`/order-item/read/${id}`);
  },

  async deleteOrderItem(id: number): Promise<void> {
    await apiDelete(`/order-item/delete/${id}`);
  },
};
