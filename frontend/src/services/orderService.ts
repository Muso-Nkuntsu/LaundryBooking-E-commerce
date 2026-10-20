import { apiGet } from "./Api";
import type { Order, OrderStatus } from "../types/Order";

export const orderService = {
    getAllOrders(): Promise<Order[]> {
        return apiGet<Order[]>("/orders");
    },

    getOrderById(id: number): Promise<Order> {
        return apiGet<Order>(`/orders/${id}`);
    },

    getOrdersByStudent(studentId: number): Promise<Order[]> {
        return apiGet<Order[]>(`/orders/student/${studentId}`);
    },

    getOrdersByStatus(status: OrderStatus): Promise<Order[]> {
        return apiGet<Order[]>(`/orders/status/${status}`);
    },
};