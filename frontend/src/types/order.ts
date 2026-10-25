export type OrderStatus = "PENDING" | "PAID" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface Order {
  orderId: number;
  orderDate: string;
  totalAmount: number;
  status: OrderStatus;
}

export const ORDER_STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: "Awaiting payment",
  PAID: "Paid",
  IN_PROGRESS: "Being prepared",
  COMPLETED: "Collected",
  CANCELLED: "Cancelled",
};

export const ORDER_STATUS_PILL: Record<OrderStatus, string> = {
  PENDING: "pill pill-warn",
  PAID: "pill pill-ok",
  IN_PROGRESS: "pill pill-info",
  COMPLETED: "pill pill-info",
  CANCELLED: "pill pill-off",
};
