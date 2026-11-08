package com.cput.laundryecommercebookingsystem.dto.admin;

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/*AdminDtos.java
 * What the admin dashboard sends and receives. Entities are never sent directly,
 * so a password hash or a lazy-loaded list can't leak into a response by accident.
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    /** Products at or below this quantity are flagged as low stock. */
    public static final int LOW_STOCK_LEVEL = 5;

    // =====================================================================
    // Students / users
    // =====================================================================

    public record AdminUserView(Long studentId, String firstName, String lastName, String email,
                                String phoneNumber, String role, boolean disabled, boolean emailVerified,
                                LocalDateTime createdAt) {
        public static AdminUserView from(Student s) {
            return new AdminUserView(s.getStudentId(), s.getFirstName(), s.getLastName(), s.getEmail(),
                    s.getPhoneNumber(), s.getRole().name(), s.isDisabled(), s.isEmailVerified(), s.getCreatedAt());
        }
    }

    public record AdminUserDetail(AdminUserView user, long bookingCount, long orderCount, double totalPaid,
                                  List<AdminBookingView> recentBookings, List<AdminOrderView> recentOrders) {
    }

    public record UpdateUserRequest(String firstName, String lastName, String email, String phoneNumber) {
    }

    public record EnabledRequest(Boolean enabled) {
    }

    public record RoleRequest(Role role) {
    }

    // =====================================================================
    // Bookings
    // =====================================================================

    public record AdminBookingView(Long bookingId, Long studentId, String studentName, String studentEmail,
                                   Long machineId, String machineNumber, String machineType, String roomNumber,
                                   Long timeSlotId, LocalDate date, LocalTime startTime, LocalTime endTime,
                                   String serviceName, double totalAmount, String status, LocalDateTime bookedAt) {
        public static AdminBookingView from(Booking b) {
            Student s = b.getStudent();
            LaundryMachine m = b.getLaundryMachine();
            TimeSlot t = b.getTimeSlot();
            LaundryService service = b.getLaundryService();
            LaundryRoom room = m == null ? null : m.getLaundryRoom();
            return new AdminBookingView(b.getId(),
                    s == null ? null : s.getStudentId(),
                    s == null ? "" : s.getFirstName() + " " + s.getLastName(),
                    s == null ? "" : s.getEmail(),
                    m == null ? null : m.getMachineId(),
                    m == null ? "" : m.getMachineNumber(),
                    m == null ? "" : m.getType(),
                    room == null ? "" : room.getRoomNumber(),
                    t == null ? null : t.getId(),
                    t == null ? null : t.getDate(),
                    t == null ? null : t.getStartTime(),
                    t == null ? null : t.getEndTime(),
                    service == null ? null : service.getServiceName(),
                    b.getTotalAmount(),
                    b.getStatus() == null ? "" : b.getStatus().name(),
                    b.getBookingDate());
        }
    }

    public record BookingStatusRequest(BookingStatus status) {
    }

    /** One day: every machine, every time slot, and the bookings that fill them. */
    public record ScheduleView(LocalDate date, List<MachineView> machines, List<TimeSlotView> slots,
                               List<AdminBookingView> bookings) {
    }

    // =====================================================================
    // Rooms, machines, time slots
    // =====================================================================

    public record RoomView(Long roomId, String roomNumber, String location, int capacity, String description,
                           boolean active, int machineCount) {
    }

    public record RoomRequest(String roomNumber, String location, Integer capacity, String description) {
    }

    public record ActiveRequest(Boolean active) {
    }

    public record MachineView(Long machineId, String machineNumber, String type, String status,
                              Long roomId, String roomNumber, long upcomingBookings) {
    }

    public record MachineRequest(String machineNumber, String type, Long roomId, MachineStatus status) {
    }

    public record MachineStatusRequest(MachineStatus status) {
    }

    /** upcomingBookingsAffected tells the admin how many bookings are on a machine they just took offline. */
    public record MachineStatusResult(MachineView machine, long upcomingBookingsAffected) {
    }

    public record TimeSlotView(Long timeSlotId, LocalDate date, LocalTime startTime, LocalTime endTime,
                               boolean available, long bookings) {
    }

    public record TimeSlotRequest(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    /** Creates every slot between dayStart and dayEnd, for each day from fromDate to toDate. */
    public record GenerateSlotsRequest(LocalDate fromDate, LocalDate toDate, LocalTime dayStart,
                                       LocalTime dayEnd, Integer slotMinutes) {
    }

    public record GenerateSlotsResult(int created, int skipped) {
    }

    public record AvailableRequest(Boolean available) {
    }

    // =====================================================================
    // Products
    // =====================================================================

    public record AdminProductView(Long productId, String name, String description, String category,
                                   double price, int inventoryQuantity, String imageUrl, boolean active,
                                   boolean lowStock, long unitsSold) {
        public static AdminProductView from(Product p, long unitsSold) {
            return new AdminProductView(p.getProductId(), p.getName(), p.getDescription(), p.getCategory(),
                    p.getPrice(), p.getInventoryQuantity(), p.getImageUrl(), p.isActive(),
                    p.getInventoryQuantity() <= LOW_STOCK_LEVEL, unitsSold);
        }
    }

    public record ProductRequest(String name, String description, String category, Double price,
                                 Integer inventoryQuantity) {
    }

    public record StockRequest(Integer inventoryQuantity) {
    }

    // =====================================================================
    // Orders and payments
    // =====================================================================

    /** paymentStatus: NONE, AWAITING_VERIFICATION, PAID or FAILED, worked out from the order's payments. */
    public record AdminOrderView(Long orderId, Long studentId, String studentName, String studentEmail,
                                 LocalDateTime orderDate, double totalAmount, String status, int itemCount,
                                 String paymentStatus) {
    }

    public record OrderLineView(Long orderItemId, Long productId, String productName, int quantity,
                                double unitPrice, double subtotal) {
        public static OrderLineView from(OrderItem i) {
            Product p = i.getProduct();
            return new OrderLineView(i.getOrderItemId(), p == null ? null : p.getProductId(),
                    p == null ? "Removed product" : p.getName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal());
        }
    }

    public record AdminOrderDetail(AdminOrderView order, String studentPhone, List<OrderLineView> items,
                                   List<AdminPaymentView> payments) {
    }

    public record OrderStatusRequest(OrderStatus status) {
    }

    /** paidFor is a short description, for example "Booking #12" or "Order #4". */
    public record AdminPaymentView(Long paymentId, double amount, LocalDateTime paymentDate, String method,
                                   String status, String transactionRef, String payerName, String payerBank,
                                   Long bookingId, Long orderId, String paidFor, Long studentId,
                                   String studentName, String studentEmail) {
    }

    public record VerifyPaymentRequest(Boolean approved) {
    }

    // =====================================================================
    // Dashboard and reports
    // =====================================================================

    public record DashboardView(
            long totalStudents, long activeStudents, long admins, long unverifiedStudents,
            long totalBookings, long pendingBookings, long confirmedBookings, long completedBookings,
            long cancelledBookings, long bookingsToday,
            long totalOrders, long unpaidOrders, long ordersToFulfil, long completedOrders,
            long paymentsToVerify, double amountToVerify,
            long lowStockProducts, long outOfStockProducts,
            long machinesAvailable, long machinesOutOfOrder, long machinesTotal,
            List<AdminBookingView> recentBookings, List<AdminOrderView> recentOrders) {
    }

    public record CountItem(String label, long count) {
    }

    public record DayPoint(LocalDate date, long count, double amount) {
    }

    public record TopProduct(String name, long quantity, double revenue) {
    }

    public record ReportView(
            int days,
            // bookings (by the day they were made)
            long bookingsMade, long bookingsCancelled, double cancellationRate,
            List<CountItem> bookingsByStatus, List<CountItem> bookingsByMachine, List<DayPoint> bookingsPerDay,
            // orders and money
            long ordersPlaced, long ordersCancelled, double paymentsReceived, double averageOrderValue,
            List<CountItem> ordersByStatus, List<DayPoint> ordersPerDay, List<DayPoint> paymentsPerDay,
            List<TopProduct> topProducts,
            // stock
            long activeProducts, long lowStockCount, long outOfStockCount, double stockValue,
            List<AdminProductView> stock,
            // activity
            long newStudents) {
    }
}
