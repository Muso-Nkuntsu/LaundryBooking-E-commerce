package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.*;
import com.cput.laundryecommercebookingsystem.repository.*;
import com.cput.laundryecommercebookingsystem.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Stream;

/**
 * Talent Nocuze
 * 230405886
 */
@Service
public class AdminService {

    static final int LOW_STOCK_LIMIT = 5;
    private static final String PENDING = "Pending";
    private static final String COMPLETED = "Completed";
    private static final String FAILED = "Failed";

    private final IStudentRepository students;
    private final iBookingRepository bookings;
    private final IOrderRepository orders;
    private final PaymentRepository payments;
    private final IProductRepository products;
    private final ILaundryMachineRepository machines;

    public AdminService(IStudentRepository students, iBookingRepository bookings, IOrderRepository orders,
                        PaymentRepository payments, IProductRepository products,
                        ILaundryMachineRepository machines) {
        this.students = students;
        this.bookings = bookings;
        this.orders = orders;
        this.payments = payments;
        this.products = products;
        this.machines = machines;
    }

    // ======================= overview & reports =======================

    @Transactional(readOnly = true)
    public Overview overview() {
        List<Student> allStudents = students.findAll();
        long lowStock = products.findAll().stream()
                .filter(Product::isActive).filter(p -> p.getInventoryQuantity() <= LOW_STOCK_LIMIT).count();
        return new Overview(
                allStudents.size(),
                allStudents.stream().filter(Student::isActive).count(),
                bookings.count(),
                bookings.countByStatus(BookingStatus.PENDING),
                bookings.countByStatus(BookingStatus.CONFIRMED),
                bookings.countByStatus(BookingStatus.COMPLETED),
                bookings.countByStatus(BookingStatus.CANCELLED),
                orders.count(),
                orders.countByStatus(OrderStatus.PENDING),
                orders.countByStatus(OrderStatus.COMPLETED),
                payments.countByStatus(PENDING),
                payments.sumAmountByStatus(COMPLETED),
                lowStock,
                bookings.findTop5ByOrderByBookingDateDesc().stream().map(BookingRow::from).toList(),
                orders.findTop5ByOrderByOrderDateDesc().stream().map(o -> toOrderRow(o, paymentsOf(o.getOrderId()))).toList());
    }

    @Transactional(readOnly = true)
    public Reports reports() {
        Map<String, Long> bookingsByStatus = new LinkedHashMap<>();
        for (BookingStatus s : BookingStatus.values()) bookingsByStatus.put(s.name(), bookings.countByStatus(s));
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) ordersByStatus.put(s.name(), orders.countByStatus(s));
        Map<String, Long> machinesByStatus = new LinkedHashMap<>();
        for (MachineStatus s : MachineStatus.values()) machinesByStatus.put(s.name(), machines.countByStatus(s));

        List<Product> allProducts = products.findAll();
        List<StockRow> stock = allProducts.stream()
                .sorted(Comparator.comparingInt(Product::getInventoryQuantity))
                .map(p -> new StockRow(p.getProductId(), p.getName(), p.getCategory(), p.getInventoryQuantity(),
                        p.isActive(), stockLevel(p.getInventoryQuantity())))
                .toList();

        List<ActivityRow> activity = Stream.of(
                        bookings.findTop5ByOrderByBookingDateDesc().stream().map(b -> new ActivityRow("BOOKING", b.getId(),
                                "Booking #" + b.getId() + " (" + b.getStatus() + ")", b.getBookingDate())),
                        orders.findTop5ByOrderByOrderDateDesc().stream().map(o -> new ActivityRow("ORDER", o.getOrderId(),
                                "Order #" + o.getOrderId() + " (" + o.getStatus() + ")", o.getOrderDate())),
                        payments.findTop10ByOrderByPaymentDateDesc().stream().map(p -> new ActivityRow("PAYMENT", p.getPaymentId(),
                                "Payment " + p.getTransactionRef() + " (" + p.getStatus() + ")", p.getPaymentDate())))
                .flatMap(s -> s)
                .sorted(Comparator.comparing(ActivityRow::at, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(15).toList();

        return new Reports(bookingsByStatus, ordersByStatus, machinesByStatus,
                payments.sumAmountByStatus(COMPLETED), payments.sumAmountByStatus(PENDING),
                allProducts.stream().mapToLong(Product::getInventoryQuantity).sum(),
                allProducts.stream().filter(p -> p.getInventoryQuantity() == 0).count(),
                allProducts.stream().filter(p -> p.getInventoryQuantity() > 0 && p.getInventoryQuantity() <= LOW_STOCK_LIMIT).count(),
                stock, activity);
    }

    static String stockLevel(int qty) {
        if (qty == 0) return "OUT";
        return qty <= LOW_STOCK_LIMIT ? "LOW" : "OK";
    }

    // ======================= users =======================

    @Transactional(readOnly = true)
    public List<UserRow> listUsers() {
        return students.findAll().stream().map(this::toUserRow).toList();
    }

    @Transactional(readOnly = true)
    public UserRow getUser(Long id) {
        return toUserRow(findStudent(id));
    }

    @Transactional
    public UserRow updateUser(Long id, UpdateUserRequest req) {
        Student old = findStudent(id);
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        students.findByEmailIgnoreCase(email).filter(s -> !s.getStudentId().equals(id)).ifPresent(s -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another account already uses this email.");
        });
        return toUserRow(students.save(copy(old).setFirstName(req.firstName().trim()).setLastName(req.lastName().trim())
                .setEmail(email).setPhoneNumber(req.phoneNumber().trim()).build()));
    }

    @Transactional
    public UserRow setUserActive(Long id, boolean active) {
        Student old = findStudent(id);
        if (!active) {
            if (id.equals(SecurityUtils.currentStudentId())) {
                throw new IllegalStateException("You cannot deactivate your own account.");
            }
            requireAnotherActiveAdmin(old);
        }
        return toUserRow(students.save(copy(old).setActive(active).build()));
    }

    @Transactional
    public UserRow setUserRole(Long id, Role role) {
        Student old = findStudent(id);
        if (id.equals(SecurityUtils.currentStudentId())) {
            throw new IllegalStateException("You cannot change your own role.");
        }
        if (role != Role.ADMIN) {
            requireAnotherActiveAdmin(old);
        }
        return toUserRow(students.save(copy(old).setRole(role).build()));
    }

    /** Refuses a change that would leave the system without any active administrator. */
    private void requireAnotherActiveAdmin(Student target) {
        if (target.getRole() != Role.ADMIN || !target.isActive()) {
            return;
        }
        long others = students.findAll().stream()
                .filter(s -> s.getRole() == Role.ADMIN && s.isActive() && !s.getStudentId().equals(target.getStudentId()))
                .count();
        if (others == 0) {
            throw new IllegalStateException("The system must keep at least one active administrator.");
        }
    }

    private static Student.Builder copy(Student s) {
        return new Student.Builder().setStudentId(s.getStudentId()).setFirstName(s.getFirstName())
                .setLastName(s.getLastName()).setEmail(s.getEmail()).setPhoneNumber(s.getPhoneNumber())
                .setPassword(s.getPassword()).setRole(s.getRole()).setActive(s.isActive())
                .setCreatedAt(s.getCreatedAt());
    }

    private UserRow toUserRow(Student s) {
        return new UserRow(s.getStudentId(), s.getFirstName(), s.getLastName(), s.getEmail(), s.getPhoneNumber(),
                s.getRole().name(), s.isActive(), s.getCreatedAt(),
                bookings.findByStudent_StudentId(s.getStudentId()).size(),
                orders.findByStudentPrimaryKey(s.getStudentId()).size());
    }

    private Student findStudent(Long id) {
        return students.findById(id).orElseThrow(() -> new NoSuchElementException("User not found."));
    }

    // ======================= orders =======================

    @Transactional(readOnly = true)
    public List<OrderRow> listOrders(String status) {
        List<Order> all = (status == null || status.isBlank())
                ? orders.findAll() : orders.findByStatus(parseOrderStatus(status));
        Map<Long, List<Payment>> byOrder = paymentsByOrder(all);
        return all.stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(o -> toOrderRow(o, byOrder.getOrDefault(o.getOrderId(), List.of()))).toList();
    }

    @Transactional(readOnly = true)
    public OrderDetail getOrder(Long id) {
        Order order = findOrder(id);
        List<Payment> orderPayments = paymentsOf(id);
        List<OrderItemRow> items = order.getOrderItems().stream()
                .map(i -> new OrderItemRow(i.getOrderItemId(),
                        i.getProduct() == null ? null : i.getProduct().getName(),
                        i.getQuantity(), i.getUnitPrice(), i.getSubtotal())).toList();
        return new OrderDetail(toOrderRow(order, orderPayments), items,
                orderPayments.stream().map(p -> toPaymentRow(p, order.getStudent())).toList());
    }

    @Transactional
    public OrderRow updateOrderStatus(Long id, String status) {
        Order order = findOrder(id);
        order.updateStatus(parseOrderStatus(status));
        Order saved = orders.save(order);
        return toOrderRow(saved, paymentsOf(id));
    }

    private OrderRow toOrderRow(Order o, List<Payment> orderPayments) {
        Student s = o.getStudent();
        return new OrderRow(o.getOrderId(), o.getOrderDate(), o.getStatus().name(), o.getTotalAmount(),
                s == null ? null : new PersonRef(s.getStudentId(), s.getFirstName() + " " + s.getLastName(), s.getEmail()),
                o.getOrderItems().size(), paymentStatus(orderPayments));
    }

    /** Completed beats Pending beats Failed; no payment at all is "Unpaid". */
    static String paymentStatus(List<Payment> list) {
        if (list.stream().anyMatch(p -> COMPLETED.equals(p.getStatus()))) return COMPLETED;
        if (list.stream().anyMatch(p -> PENDING.equals(p.getStatus()))) return PENDING;
        if (list.stream().anyMatch(p -> FAILED.equals(p.getStatus()))) return FAILED;
        return "Unpaid";
    }

    private Order findOrder(Long id) {
        return orders.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found."));
    }

    private static OrderStatus parseOrderStatus(String status) {
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status.");
        }
    }

    private List<Payment> paymentsOf(Long orderId) {
        return payments.findByOrderId(orderId);
    }

    private Map<Long, List<Payment>> paymentsByOrder(List<Order> list) {
        if (list.isEmpty()) return Map.of();
        List<Long> ids = list.stream().map(Order::getOrderId).toList();
        Map<Long, List<Payment>> map = new HashMap<>();
        for (Payment p : payments.findByOrderIdIn(ids)) {
            map.computeIfAbsent(p.getOrderId(), k -> new ArrayList<>()).add(p);
        }
        return map;
    }

    // ======================= payments =======================

    @Transactional(readOnly = true)
    public List<PaymentRow> listPayments(String status) {
        List<Payment> list = (status == null || status.isBlank()) ? payments.findAll() : payments.findByStatus(status);
        return list.stream()
                .sorted(Comparator.comparing(Payment::getPaymentDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(p -> toPaymentRow(p, ownerOf(p))).toList();
    }

    /**
     * Verifies a pending payment (for EFT: the admin has seen the money in the bank account).
     * The payment becomes Completed, the order is marked PAID and the booking CONFIRMED.
     */
    @Transactional
    public PaymentRow verifyPayment(Long paymentId) {
        Payment payment = findPendingPayment(paymentId);
        Payment done = payments.save(rebuild(payment, COMPLETED));
        if (done.getOrderId() != null) {
            orders.findById(done.getOrderId()).ifPresent(o -> {
                if (o.getStatus() == OrderStatus.PENDING) {
                    o.updateStatus(OrderStatus.PAID);
                    orders.save(o);
                }
            });
        }
        if (done.getBookingId() != null) {
            bookings.findById(done.getBookingId()).ifPresent(b -> {
                if (b.getStatus() == BookingStatus.PENDING) {
                    b.updateStatus(BookingStatus.CONFIRMED);
                    bookings.save(b);
                }
            });
        }
        return toPaymentRow(done, ownerOf(done));
    }

    @Transactional
    public PaymentRow rejectPayment(Long paymentId) {
        Payment failed = payments.save(rebuild(findPendingPayment(paymentId), FAILED));
        return toPaymentRow(failed, ownerOf(failed));
    }

    private Payment findPendingPayment(Long id) {
        Payment p = payments.findById(id).orElseThrow(() -> new NoSuchElementException("Payment not found."));
        if (!PENDING.equals(p.getStatus())) {
            throw new IllegalStateException("Only a pending payment can be changed.");
        }
        return p;
    }

    private static Payment rebuild(Payment e, String status) {
        return Payment.builder().setPaymentId(e.getPaymentId()).setAmount(e.getAmount())
                .setPaymentDate(e.getPaymentDate()).setPaymentMethod(e.getPaymentMethod()).setStatus(status)
                .setTransactionRef(e.getTransactionRef()).setBookingId(e.getBookingId())
                .setOrderId(e.getOrderId()).setService(e.getService()).build();
    }

    private Student ownerOf(Payment p) {
        if (p.getOrderId() != null) {
            return orders.findById(p.getOrderId()).map(Order::getStudent).orElse(null);
        }
        if (p.getBookingId() != null) {
            return bookings.findById(p.getBookingId()).map(Booking::getStudent).orElse(null);
        }
        return null;
    }

    private static PaymentRow toPaymentRow(Payment p, Student owner) {
        return new PaymentRow(p.getPaymentId(), p.getAmount(), p.getPaymentDate(), p.getPaymentMethod(),
                p.getStatus(), p.getTransactionRef(), p.getBookingId(), p.getOrderId(),
                owner == null ? null : new PersonRef(owner.getStudentId(),
                        owner.getFirstName() + " " + owner.getLastName(), owner.getEmail()));
    }
}
