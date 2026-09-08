import type { OrderItem } from "../types/orderItem";

const API_BASE_URL = "http://localhost:8080";

export const orderItemService = {

  async getAllOrderItems(): Promise<OrderItem[]> {
    const response = await fetch(
      `${API_BASE_URL}/order-item/getall`
    );

    if (!response.ok) {
      throw new Error("Failed to load order items");
    }

    return response.json();
  },


  async getOrderItemById(
    id: number
  ): Promise<OrderItem> {

    const response = await fetch(
      `${API_BASE_URL}/order-item/read/${id}`
    );

    if (!response.ok) {
      throw new Error("Failed to load order item");
    }

    return response.json();
  },


  async deleteOrderItem(
    id: number
  ): Promise<void> {

    const response = await fetch(
      `${API_BASE_URL}/order-item/delete/${id}`,
      {
        method: "DELETE",
      }
    );

    if (!response.ok) {
      throw new Error("Failed to delete order item");
    }
  },
};