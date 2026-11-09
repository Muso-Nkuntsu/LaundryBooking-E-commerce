import { apiDelete, apiGet, apiPatch, apiPost, apiPut, apiUpload } from "./Api";
import type {
    AdminBooking,
    AdminMachine,
    AdminOrder,
    AdminOrderDetail,
    AdminOverview,
    AdminPayment,
    AdminProduct,
    AdminReports,
    AdminRoom,
    AdminUser,
    BookingStatus,
    DeleteResult,
    MachineStatus,
    OrderStatus,
    ProductInput,
    UpdateUserInput,
} from "../types/admin";

// Every call below is protected on the SERVER: a non-admin gets 401/403 even if they call the URL directly.
export const adminService = {
    // ---------- overview & reports ----------
    getOverview: () => apiGet<AdminOverview>("/api/admin/overview"),
    getReports: () => apiGet<AdminReports>("/api/admin/reports"),

    // ---------- users ----------
    getUsers: () => apiGet<AdminUser[]>("/api/admin/users"),
    getUser: (id: number) => apiGet<AdminUser>(`/api/admin/users/${id}`),
    updateUser: (id: number, data: UpdateUserInput) => apiPut<AdminUser>(`/api/admin/users/${id}`, data),
    setUserActive: (id: number, active: boolean) =>
        apiPut<AdminUser>(`/api/admin/users/${id}/active`, { active }),
    setUserRole: (id: number, role: "USER" | "ADMIN") =>
        apiPut<AdminUser>(`/api/admin/users/${id}/role`, { role }),

    // ---------- bookings ----------
    getBookings: () => apiGet<AdminBooking[]>("/api/admin/bookings"),
    updateBookingStatus: (id: number, status: BookingStatus) =>
        apiPut<AdminBooking>(`/api/admin/bookings/${id}/status`, { status }),

    // ---------- machines (existing admin-only write endpoints) ----------
    getMachines: () => apiGet<AdminMachine[]>("/laundrymachine/getall"),
    getRooms: () => apiGet<AdminRoom[]>("/laundry-room/all"),
    createMachine: (machineNumber: string, type: string, status: MachineStatus, laundryRoomId: number) =>
        apiPost<AdminMachine>("/laundrymachine/create", undefined, {
            machineNumber,
            type,
            status,
            laundryRoomId,
        }),
    updateMachineStatus: (id: number, status: MachineStatus) =>
        apiPatch<AdminMachine>(`/laundrymachine/update-status/${id}`, undefined, { status }),
    deleteMachine: (id: number) => apiDelete<void>(`/laundrymachine/delete/${id}`),

    // ---------- products ----------
    getProducts: () => apiGet<AdminProduct[]>("/api/admin/products"),
    createProduct: (data: ProductInput) => apiPost<AdminProduct>("/api/admin/products", toProductBody(data)),
    updateProduct: (id: number, data: ProductInput) =>
        apiPut<AdminProduct>(`/api/admin/products/${id}`, toProductBody(data)),
    setProductStock: (id: number, inventoryQuantity: number) =>
        apiPut<AdminProduct>(`/api/admin/products/${id}/stock`, { inventoryQuantity }),
    setProductActive: (id: number, active: boolean) =>
        apiPut<AdminProduct>(`/api/admin/products/${id}/active`, { active }),
    uploadProductImage: (id: number, file: File) => {
        const form = new FormData();
        form.append("file", file);
        return apiUpload<AdminProduct>(`/api/admin/products/${id}/image`, form);
    },
    deleteProduct: (id: number) => apiDelete<DeleteResult>(`/api/admin/products/${id}`),

    // ---------- orders & payments ----------
    getOrders: (status?: OrderStatus) =>
        apiGet<AdminOrder[]>("/api/admin/orders", status ? { status } : undefined),
    getOrder: (id: number) => apiGet<AdminOrderDetail>(`/api/admin/orders/${id}`),
    updateOrderStatus: (id: number, status: OrderStatus) =>
        apiPut<AdminOrder>(`/api/admin/orders/${id}/status`, { status }),
    getPayments: (status?: string) =>
        apiGet<AdminPayment[]>("/api/admin/payments", status ? { status } : undefined),
    verifyPayment: (id: number) => apiPut<AdminPayment>(`/api/admin/payments/${id}/verify`),
    rejectPayment: (id: number) => apiPut<AdminPayment>(`/api/admin/payments/${id}/reject`),
};

const toProductBody = (data: ProductInput) => ({
    name: data.name.trim(),
    description: data.description.trim(),
    price: data.price,
    category: data.category.trim(),
    inventoryQuantity: data.inventoryQuantity,
    imageUrl: data.imageUrl.trim(),
    active: data.active,
});