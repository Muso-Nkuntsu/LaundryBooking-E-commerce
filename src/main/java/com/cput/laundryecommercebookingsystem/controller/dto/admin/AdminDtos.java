package com.cput.laundryecommercebookingsystem.controller.dto.admin;

import com.cput.laundryecommercebookingsystem.domain.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Talent Nocuze
 * 230405886
 */
public final class AdminDtos {

    private AdminDtos() {
    }

    // ---------------- overview ----------------

    public record Overview(long totalUsers, long activeUsers, long totalBookings, long pendingBookings,
                           long confirmedBookings, long completedBookings, long cancelledBookings,
                           long totalOrders, long pendingOrders, long completedOrders,
                           long pendingPayments, double revenue, long lowStockProducts,
                           List<BookingRow> recentBookings, List<OrderRow> recentOrders) {
    }

    public record Reports(Map<String, Long> bookingsByStatus, Map<String, Long> ordersByStatus,
                          Map<String, Long> machinesByStatus, double revenueCompleted, double revenuePending,
                          long totalStockUnits, long outOfStockProducts, long lowStockProducts,
                          List<StockRow> stock, List<ActivityRow> recentActivity) {
    }

    public record StockRow(Long productId, String name, String category, int inventoryQuantity,
                           boolean active, String level) {
    }

    public record ActivityRow(String type, Long id, String description, LocalDateTime at) {
    }

    // ---------------- users ----------------

    public record UserRow(Long studentId, String firstName, String lastName, String email, String phoneNumber,
                          String role, boolean active, LocalDateTime createdAt, long bookingCount, long orderCount) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "First name is required.") @Size(max = 50, message = "First name is too long.") String firstName,
            @NotBlank(message = "Last name is required.") @Size(max = 50, message = "Last name is too long.") String lastName,
            @NotBlank(message = "Email is required.") @Email(message = "Enter a valid email address.")
            @Size(max = 254, message = "Email is too long.") String email,
            @NotBlank(message = "Phone number is required.")
            @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Enter a valid phone number.") String phoneNumber) {
    }

    public record ActiveRequest(@NotNull(message = "active is required.") Boolean active) {
    }

    public record RoleRequest(@NotBlank(message = "Role is required.")
                              @Pattern(regexp = "USER|ADMIN", message = "Role must be USER or ADMIN.") String role) {
    }

    // ---------------- bookings ----------------

    public record PersonRef(Long studentId, String name, String email) {
    }

    public record BookingRow(Long id, LocalDateTime bookingDate, String status, double totalAmount,
                             PersonRef student, Long machineId, String machineNumber, String machineStatus,
                             String roomNumber, LocalDate slotDate, LocalTime startTime, LocalTime endTime,
                             String serviceName) {

        public static BookingRow from(Booking b) {
            Student s = b.getStudent();
            LaundryMachine m = b.getLaundryMachine();
            TimeSlot t = b.getTimeSlot();
            LaundryRoom room = m == null ? null : m.getLaundryRoom();
            return new BookingRow(
                    b.getId(), b.getBookingDate(), b.getStatus() == null ? null : b.getStatus().name(),
                    b.getTotalAmount(),
                    s == null ? null : new PersonRef(s.getStudentId(), s.getFirstName() + " " + s.getLastName(), s.getEmail()),
                    m == null ? null : m.getMachineId(), m == null ? null : m.getMachineNumber(),
                    m == null || m.getStatus() == null ? null : m.getStatus().name(),
                    room == null ? null : room.getRoomNumber(),
                    t == null ? null : t.getDate(), t == null ? null : t.getStartTime(), t == null ? null : t.getEndTime(),
                    b.getLaundryService() == null ? null : b.getLaundryService().getServiceName());
        }
    }

    // ---------------- products ----------------

    public record ProductRequest(
            @NotBlank(message = "Name is required.") @Size(max = 120, message = "Name is too long.") String name,
            @Size(max = 1000, message = "Description is too long.") String description,
            @NotNull(message = "Price is required.") @DecimalMin(value = "0.0", message = "Price cannot be negative.")
            @DecimalMax(value = "1000000.0", message = "Price is too large.") Double price,
            @Size(max = 60, message = "Category is too long.") String category,
            @NotNull(message = "Stock quantity is required.") @Min(value = 0, message = "Stock cannot be negative.")
            @Max(value = 1000000, message = "Stock is too large.") Integer inventoryQuantity,
            @Size(max = 500, message = "Image URL is too long.") String imageUrl,
            Boolean active) {
    }

    public record StockRequest(@NotNull(message = "Stock quantity is required.")
                               @Min(value = 0, message = "Stock cannot be negative.")
                               @Max(value = 1000000, message = "Stock is too large.") Integer inventoryQuantity) {
    }

    public record ProductRow(Long productId, String name, String description, double price, String category,
                             int inventoryQuantity, String imageUrl, boolean active) {

        public static ProductRow from(Product p) {
            return new ProductRow(p.getProductId(), p.getName(), p.getDescription(), p.getPrice(), p.getCategory(),
                    p.getInventoryQuantity(), p.getImageUrl(), p.isActive());
        }
    }

    public record DeleteResult(boolean deleted, boolean deactivated, String message) {
    }

    // ---------------- orders and payments ----------------

    public record OrderItemRow(Long orderItemId, String productName, int quantity, double unitPrice, double subtotal) {
    }

    public record PaymentRow(Long paymentId, double amount, LocalDateTime paymentDate, String paymentMethod,
                             String status, String transactionRef, Long bookingId, Long orderId,
                             PersonRef customer) {
    }

    public record OrderRow(Long orderId, LocalDateTime orderDate, String status, double totalAmount,
                           PersonRef student, int itemCount, String paymentStatus) {
    }

    public record OrderDetail(OrderRow order, List<OrderItemRow> items, List<PaymentRow> payments) {
    }

    public record OrderStatusRequest(@NotBlank(message = "Status is required.")
                                     @Pattern(regexp = "PENDING|PAID|IN_PROGRESS|COMPLETED|CANCELLED",
                                             message = "Invalid order status.") String status) {
    }

    public record BookingStatusRequest(@NotBlank(message = "Status is required.")
                                       @Pattern(regexp = "PENDING|CONFIRMED|COMPLETED|CANCELLED",
                                               message = "Invalid booking status.") String status) {
    }

