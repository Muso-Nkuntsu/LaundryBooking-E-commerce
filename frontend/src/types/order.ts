export type OrderStatus = "PENDING" | "PLACED" | "COMPLETED" | "CANCELLED";

export interface Order {
    orderId: number;
    orderDate: string;
    totalAmount: number;
    status: OrderStatus;
    studentId: number;
    paymentStatus?: string;
}