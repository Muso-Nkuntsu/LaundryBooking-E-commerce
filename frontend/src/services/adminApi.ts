// Every call the admin dashboard makes. All of these are under /api/admin and the backend
// refuses them (403) unless the logged-in account is an ADMIN.
import { apiDelete, apiGet, apiPatch, apiPost, apiPut, apiUpload } from "./Api";

// ---------- Types (they match backend dto/admin/AdminDtos.java) ----------

export type Role = "USER" | "ADMIN";
export type BookingStatus = "PENDING" | "CONFIRMED" | "COMPLETED" | "CANCELLED";
export type OrderStatus = "PENDING" | "PAID" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
export type MachineStatus = "AVAILABLE" | "IN_USE" | "OUT_OF_ORDER";
export type PaymentStatus = "PENDING" | "COMPLETED" | "FAILED";

export interface AdminUser {
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: Role;
  disabled: boolean;
  emailVerified: boolean;
  createdAt: string | null;
}

export interface AdminUserDetail {
  user: AdminUser;
  bookingCount: number;
  orderCount: number;
  totalPaid: number;
  recentBookings: AdminBooking[];
  recentOrders: AdminOrder[];
}

export interface AdminBooking {
  bookingId: number;
  studentId: number | null;
  studentName: string;
  studentEmail: string;
  machineId: number | null;
  machineNumber: string;
  machineType: string;
  roomNumber: string;
  timeSlotId: number | null;
  date: string | null;
  startTime: string | null;
  endTime: string | null;
  serviceName: string | null;
  totalAmount: number;
  status: BookingStatus;
  bookedAt: string | null;
}

export interface Room {
  roomId: number;
  roomNumber: string;
  location: string;
  capacity: number;
  description: string | null;
  active: boolean;
  machineCount: number;
}

export interface Machine {
  machineId: number;
  machineNumber: string;
  type: string;
  status: MachineStatus;
  roomId: number | null;
  roomNumber: string;
  upcomingBookings: number;
}

export interface TimeSlot {
  timeSlotId: number;
  date: string;
  startTime: string;
  endTime: string;
  available: boolean;
  bookings: number;
}

export interface Schedule {
  date: string;
  machines: Machine[];
  slots: TimeSlot[];
  bookings: AdminBooking[];
}

export interface AdminProduct {
  productId: number;
  name: string;
  description: string | null;
  category: string | null;
  price: number;
  inventoryQuantity: number;
  imageUrl: string | null;
  active: boolean;
  lowStock: boolean;
  unitsSold: number;
}

export interface AdminOrder {
  orderId: number;
  studentId: number | null;
  studentName: string;
  studentEmail: string;
  orderDate: string | null;
  totalAmount: number;
  status: OrderStatus;
  itemCount: number;
  /** NONE, AWAITING_VERIFICATION, PAID or FAILED, worked out from the order's payments */
  paymentStatus: string;
}

export interface OrderLine {
  orderItemId: number;
  productId: number | null;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface AdminPayment {
  paymentId: number;
  amount: number;
  paymentDate: string | null;
  method: string;
  status: PaymentStatus;
  transactionRef: string | null;
  payerName: string | null;
  payerBank: string | null;
  bookingId: number | null;
  orderId: number | null;
  paidFor: string;
  studentId: number | null;
  studentName: string;
  studentEmail: string;
}

export interface AdminOrderDetail {
  order: AdminOrder;
  studentPhone: string;
  items: OrderLine[];
  payments: AdminPayment[];
}

export interface Dashboard {
  totalStudents: number;
  activeStudents: number;
  admins: number;
  unverifiedStudents: number;
  totalBookings: number;
  pendingBookings: number;
  confirmedBookings: number;
  completedBookings: number;
  cancelledBookings: number;
  bookingsToday: number;
  totalOrders: number;
  unpaidOrders: number;
  ordersToFulfil: number;
  completedOrders: number;
  paymentsToVerify: number;
  amountToVerify: number;
  lowStockProducts: number;
  outOfStockProducts: number;
  machinesAvailable: number;
  machinesOutOfOrder: number;
  machinesTotal: number;
  recentBookings: AdminBooking[];
  recentOrders: AdminOrder[];
}

export interface CountItem { label: string; count: number; }
export interface DayPoint { date: string; count: number; amount: number; }
export interface TopProduct { name: string; quantity: number; revenue: number; }

export interface Report {
  days: number;
  bookingsMade: number;
  bookingsCancelled: number;
  cancellationRate: number;
  bookingsByStatus: CountItem[];
  bookingsByMachine: CountItem[];
  bookingsPerDay: DayPoint[];
  ordersPlaced: number;
  ordersCancelled: number;
  paymentsReceived: number;
  averageOrderValue: number;
  ordersByStatus: CountItem[];
  ordersPerDay: DayPoint[];
  paymentsPerDay: DayPoint[];
  topProducts: TopProduct[];
  activeProducts: number;
  lowStockCount: number;
  outOfStockCount: number;
  stockValue: number;
  stock: AdminProduct[];
  newStudents: number;
}

export interface UserInput { firstName: string; lastName: string; email: string; phoneNumber: string; }
export interface RoomInput { roomNumber: string; location: string; capacity: number; description: string; }
export interface MachineInput { machineNumber: string; type: string; roomId: number; status: MachineStatus; }
export interface ProductInput { name: string; description: string; category: string; price: number; inventoryQuantity?: number; }
export interface GenerateSlotsInput { fromDate: string; toDate: string; dayStart: string; dayEnd: string; slotMinutes: number; }

type Query = Record<string, string | number | undefined | null>;

/** Leaves out empty filters so the backend gets only what was chosen. */
const clean = (params: Query): Query =>
  Object.fromEntries(Object.entries(params).filter(([, value]) => value !== "" && value !== undefined && value !== null));

// ---------- Calls ----------

export const adminApi = {
  dashboard: () => apiGet<Dashboard>("/api/admin/dashboard"),
  report: (days: number) => apiGet<Report>("/api/admin/reports", { days }),

  // Students
  users: (filters: Query) => apiGet<AdminUser[]>("/api/admin/users", clean(filters)),
  user: (id: number) => apiGet<AdminUserDetail>(`/api/admin/users/${id}`),
  updateUser: (id: number, body: UserInput) => apiPut<AdminUser>(`/api/admin/users/${id}`, body),
  setUserEnabled: (id: number, enabled: boolean) => apiPatch<AdminUser>(`/api/admin/users/${id}/enabled`, { enabled }),
  setUserRole: (id: number, role: Role) => apiPatch<AdminUser>(`/api/admin/users/${id}/role`, { role }),
  verifyUserEmail: (id: number) => apiPatch<AdminUser>(`/api/admin/users/${id}/verify-email`),

  // Bookings
  bookings: (filters: Query) => apiGet<AdminBooking[]>("/api/admin/bookings", clean(filters)),
  schedule: (date: string) => apiGet<Schedule>("/api/admin/bookings/schedule", { date }),
  setBookingStatus: (id: number, status: BookingStatus) =>
    apiPatch<AdminBooking>(`/api/admin/bookings/${id}/status`, { status }),

  // Rooms
  rooms: () => apiGet<Room[]>("/api/admin/rooms"),
  createRoom: (body: RoomInput) => apiPost<Room>("/api/admin/rooms", body),
  updateRoom: (id: number, body: RoomInput) => apiPut<Room>(`/api/admin/rooms/${id}`, body),
  setRoomActive: (id: number, active: boolean) => apiPatch<Room>(`/api/admin/rooms/${id}/active`, { active }),
  deleteRoom: (id: number) => apiDelete(`/api/admin/rooms/${id}`),

  // Machines
  machines: () => apiGet<Machine[]>("/api/admin/machines"),
  createMachine: (body: MachineInput) => apiPost<Machine>("/api/admin/machines", body),
  updateMachine: (id: number, body: MachineInput) => apiPut<Machine>(`/api/admin/machines/${id}`, body),
  setMachineStatus: (id: number, status: MachineStatus) =>
    apiPatch<{ machine: Machine; upcomingBookingsAffected: number }>(`/api/admin/machines/${id}/status`, { status }),
  deleteMachine: (id: number) => apiDelete(`/api/admin/machines/${id}`),

  // Time slots
  timeSlots: (from: string, to: string) => apiGet<TimeSlot[]>("/api/admin/timeslots", clean({ from, to })),
  createSlot: (body: { date: string; startTime: string; endTime: string }) => apiPost<TimeSlot>("/api/admin/timeslots", body),
  generateSlots: (body: GenerateSlotsInput) =>
    apiPost<{ created: number; skipped: number }>("/api/admin/timeslots/generate", body),
  setSlotAvailable: (id: number, available: boolean) =>
    apiPatch<TimeSlot>(`/api/admin/timeslots/${id}/available`, { available }),
  deleteSlot: (id: number) => apiDelete(`/api/admin/timeslots/${id}`),

  // Products
  products: (filters: Query) => apiGet<AdminProduct[]>("/api/admin/products", clean(filters)),
  categories: () => apiGet<string[]>("/api/admin/products/categories"),
  createProduct: (body: ProductInput) => apiPost<AdminProduct>("/api/admin/products", body),
  updateProduct: (id: number, body: ProductInput) => apiPut<AdminProduct>(`/api/admin/products/${id}`, body),
  setStock: (id: number, inventoryQuantity: number) =>
    apiPatch<AdminProduct>(`/api/admin/products/${id}/stock`, { inventoryQuantity }),
  setProductActive: (id: number, active: boolean) => apiPatch<AdminProduct>(`/api/admin/products/${id}/active`, { active }),
  deleteProduct: (id: number) => apiDelete(`/api/admin/products/${id}`),
  uploadProductImage: (id: number, file: File) => {
    const form = new FormData();
    form.append("file", file);
    return apiUpload<AdminProduct>(`/api/admin/products/${id}/image`, form);
  },
  removeProductImage: (id: number) => apiDelete<AdminProduct>(`/api/admin/products/${id}/image`),

  // Orders and payments
  orders: (filters: Query) => apiGet<AdminOrder[]>("/api/admin/orders", clean(filters)),
  order: (id: number) => apiGet<AdminOrderDetail>(`/api/admin/orders/${id}`),
  setOrderStatus: (id: number, status: OrderStatus) =>
    apiPatch<AdminOrderDetail>(`/api/admin/orders/${id}/status`, { status }),
  payments: (filters: Query) => apiGet<AdminPayment[]>("/api/admin/payments", clean(filters)),
  verifyPayment: (id: number, approved: boolean) =>
    apiPatch<AdminPayment>(`/api/admin/payments/${id}/verify`, { approved }),
};

// ---------- The same status rules as the backend (the backend still enforces them) ----------

export const BOOKING_NEXT: Record<BookingStatus, BookingStatus[]> = {
  PENDING: ["CONFIRMED", "CANCELLED"],
  CONFIRMED: ["COMPLETED", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
};

export const ORDER_NEXT: Record<OrderStatus, OrderStatus[]> = {
  PENDING: ["PAID", "CANCELLED"],
  PAID: ["IN_PROGRESS", "CANCELLED"],
  IN_PROGRESS: ["COMPLETED", "CANCELLED"],
  COMPLETED: [],
  CANCELLED: [],
};

export const MACHINE_TYPES = ["Washer", "Dryer"];
