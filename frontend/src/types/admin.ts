

export type BookingStatus = "PENDING" | "CONFIRMED" | "COMPLETED" | "CANCELLED";
export type OrderStatus = "PENDING" | "PAID" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
export type MachineStatus = "AVAILABLE" | "IN_USE" | "OUT_OF_ORDER";

export interface PersonRef {
    studentId: number;
    name: string;
    email: string;
}

export interface AdminBooking {
    id: number;
    bookingDate: string | null;
    status: BookingStatus;
    totalAmount: number;
    student: PersonRef | null;
    machineId: number | null;
    machineNumber: string | null;
    machineStatus: MachineStatus | null;
    roomNumber: string | null;
    slotDate: string | null;
    startTime: string | null;
    endTime: string | null;
    serviceName: string | null;
}

export interface AdminOrder {
    orderId: number;
    orderDate: string | null;
    status: OrderStatus;
    totalAmount: number;
    student: PersonRef | null;
    itemCount: number;
    paymentStatus: "Completed" | "Pending" | "Failed" | "Unpaid";
}

export interface AdminOrderItem {
    orderItemId: number;
    productName: string | null;
    quantity: number;
    unitPrice: number;
    subtotal: number;
}

export interface AdminPayment {
    paymentId: number;
    amount: number;
    paymentDate: string | null;
    paymentMethod: string;
    status: "Pending" | "Completed" | "Failed" | string;
    transactionRef: string;
    bookingId: number | null;
    orderId: number | null;
    customer: PersonRef | null;
}

export interface AdminOrderDetail {
    order: AdminOrder;
    items: AdminOrderItem[];
    payments: AdminPayment[];
}

export interface AdminOverview {
    totalUsers: number;
    activeUsers: number;
    totalBookings: number;
    pendingBookings: number;
    confirmedBookings: number;
    completedBookings: number;
    cancelledBookings: number;
    totalOrders: number;
    pendingOrders: number;
    completedOrders: number;
    pendingPayments: number;
    revenue: number;
    lowStockProducts: number;
    recentBookings: AdminBooking[];
    recentOrders: AdminOrder[];
}

export interface StockRow {
    productId: number;
    name: string;
    category: string | null;
    inventoryQuantity: number;
    active: boolean;
    level: "OK" | "LOW" | "OUT";
}

export interface ActivityRow {
    type: "BOOKING" | "ORDER" | "PAYMENT";
    id: number;
    description: string;
    at: string | null;
}

export interface AdminReports {
    bookingsByStatus: Record<string, number>;
    ordersByStatus: Record<string, number>;
    machinesByStatus: Record<string, number>;
    revenueCompleted: number;
    revenuePending: number;
    totalStockUnits: number;
    outOfStockProducts: number;
    lowStockProducts: number;
    stock: StockRow[];
    recentActivity: ActivityRow[];
}

export interface AdminUser {
    studentId: number;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    role: "USER" | "ADMIN";
    active: boolean;
    createdAt: string | null;
    bookingCount: number;
    orderCount: number;
}

export interface UpdateUserInput {
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
}

export interface AdminProduct {
    productId: number;
    name: string;
    description: string | null;
    price: number;
    category: string | null;
    inventoryQuantity: number;
    imageUrl: string | null;
    active: boolean;
}

export interface ProductInput {
    name: string;
    description: string;
    price: number;
    category: string;
    inventoryQuantity: number;
    imageUrl: string;
    active: boolean;
}

export interface DeleteResult {
    deleted: boolean;
    deactivated: boolean;
    message: string;
}

export interface AdminMachine {
    machineId: number;
    machineNumber: string;
    type: string;
    status: MachineStatus;
    laundryRoom?: { roomId: number; roomNumber: string } | null;
}

export interface AdminRoom {
    roomId: number;
    roomNumber: string;
    location?: string;
}