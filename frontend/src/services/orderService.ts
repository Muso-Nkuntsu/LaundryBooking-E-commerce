import { apiGet, apiPatch, apiPost } from "./Api";
import type { Order, OrderStatus } from "../types/Order";

// The backend's order endpoints live under /api/orders.
export const orderService = {
    /** Turns the cart into a PENDING order. The backend sets the prices and holds the stock. */
    checkout(studentId: number, items: { productId: number; quantity: number }[]): Promise<Order> {
        return apiPost<Order>("/api/orders/checkout", { studentId, items });
    },

    /** Cancels a PENDING order and puts the held stock back. */
    cancelOrder(id: number): Promise<Order> {
        return apiPatch<Order>(`/api/orders/${id}/cancel`);
    },

    getOrderById(id: number): Promise<Order> {
        return apiGet<Order>(`/api/orders/${id}`);
    },

    getOrdersByStudent(studentId: number): Promise<Order[]> {
        return apiGet<Order[]>(`/api/orders/student/${studentId}`);
    },

    /** Admin only. */
    getAllOrders(): Promise<Order[]> {
        return apiGet<Order[]>("/api/orders");
    },

    /** Admin only. */
    getOrdersByStatus(status: OrderStatus): Promise<Order[]> {
        return apiGet<Order[]>(`/api/orders/status/${status}`);
    },
};
