package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.repository.IOrderItemRepository;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import com.cput.laundryecommercebookingsystem.repository.PaymentRepository;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import com.cput.laundryecommercebookingsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus.*;

/*AdminOrderService.java
 * Admin dashboard: shop orders and all payments.
 * - Orders move PENDING -> PAID -> IN_PROGRESS -> COMPLETED; cancelling puts the stock back.
 * - EFT payments wait as PENDING until an admin checks the bank and approves (or rejects) them.
 *   Approving uses PaymentServiceImpl.markAsCompleted, which also confirms the booking or marks the order PAID.
 */
@Service
public class AdminOrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> NEXT = Map.of(
            PENDING, Set.of(PAID, CANCELLED),          // PAID by hand, e.g. paid in cash at the office
            PAID, Set.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, Set.of(COMPLETED, CANCELLED),
            COMPLETED, Set.of(),
            CANCELLED, Set.of()
    );

    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final IOrderService orderService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final iBookingRepository bookingRepository;

    public AdminOrderService(IOrderRepository orderRepository, IOrderItemRepository orderItemRepository,
                             IOrderService orderService, PaymentRepository paymentRepository,
                             PaymentService paymentService, iBookingRepository bookingRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderService = orderService;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
        this.bookingRepository = bookingRepository;
    }

    // =====================================================================
    // Orders
    // =====================================================================

    /**
     * @param status OrderStatus name, or "OUTSTANDING" for orders still to fulfil (PAID or IN_PROGRESS)
     */
    @Transactional(readOnly = true)
    public List<AdminOrderView> listOrders(String status, String q) {
        String search = q == null ? "" : q.trim().toLowerCase();
        Map<Long, Integer> itemCounts = orderItemRepository.findAll().stream()
                .filter(i -> i.getOrder() != null)
                .collect(Collectors.groupingBy(i -> i.getOrder().getOrderId(), Collectors.summingInt(OrderItem::getQuantity)));
        Map<Long, List<Payment>> paymentsByOrder = paymentRepository.findAll().stream()
                .filter(p -> p.getOrderId() != null)
                .collect(Collectors.groupingBy(Payment::getOrderId));

        return orderRepository.findAll().stream()
                .filter(o -> matchesStatus(o, status))
                .filter(o -> search.isEmpty() || matches(o, search))
                .sorted(Comparator.comparing(Order::getOrderId).reversed())
                .map(o -> toView(o, itemCounts.getOrDefault(o.getOrderId(), 0),
                        paymentsByOrder.getOrDefault(o.getOrderId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminOrderView> recentOrders(int limit) {
        return listOrders(null, null).stream().limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public AdminOrderDetail getOrder(Long id) {
        Order order = findOrder(id);
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        List<Payment> payments = paymentRepository.findByOrderId(id);
        int count = items.stream().mapToInt(OrderItem::getQuantity).sum();
        Map<Long, Order> orders = Map.of(id, order);
        return new AdminOrderDetail(
                toView(order, count, payments),
                order.getStudent() == null ? "" : order.getStudent().getPhoneNumber(),
                items.stream().map(OrderLineView::from).toList(),
                payments.stream().map(p -> toPaymentView(p, Map.of(), orders)).toList());
    }

    public Set<OrderStatus> nextStatuses(OrderStatus current) {
        return NEXT.getOrDefault(current, Set.of());
    }

    @Transactional
    public AdminOrderDetail updateStatus(Long id, OrderStatus target) {
        if (target == null) {
            throw new IllegalArgumentException("Choose a status");
        }
        Order order = findOrder(id);
        if (order.getStatus() != target) {
            if (!nextStatuses(order.getStatus()).contains(target)) {
                throw new IllegalStateException("A " + order.getStatus() + " order can't be changed to " + target);
            }
            switch (target) {
                case CANCELLED -> orderService.cancelOrder(id); // puts the stock back
                case PAID -> orderService.placeOrder(id);
                default -> orderService.updateOrderStatus(id, target);
            }
        }
        return getOrder(id);
    }

    // =====================================================================
    // Payments
    // =====================================================================

    /** Newest first. status: PENDING, COMPLETED or FAILED; method: CARD or EFT (both optional). */
    @Transactional(readOnly = true)
    public List<AdminPaymentView> listPayments(String status, String method, String q) {
        String search = q == null ? "" : q.trim().toLowerCase();
        Map<Long, Booking> bookings = bookingRepository.findAll().stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity()));
        Map<Long, Order> orders = orderRepository.findAll().stream()
                .collect(Collectors.toMap(Order::getOrderId, Function.identity()));

        return paymentRepository.findAll().stream()
                .filter(p -> status == null || status.isBlank() || status.equalsIgnoreCase(p.getStatus()))
                .filter(p -> method == null || method.isBlank() || method.equalsIgnoreCase(p.getPaymentMethod()))
                .sorted(Comparator.comparing(Payment::getPaymentId).reversed())
                .map(p -> toPaymentView(p, bookings, orders))
                .filter(v -> search.isEmpty() || (v.studentName() + " " + v.studentEmail() + " "
                        + nullToEmpty(v.transactionRef()) + " " + nullToEmpty(v.payerName()) + " " + v.paidFor())
                        .toLowerCase().contains(search))
                .toList();
    }

    /** Approve: the money is in the bank. Reject: it never arrived or the details don't match. */
    @Transactional
    public AdminPaymentView verifyPayment(Long id, boolean approved) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Payment not found with id: " + id));
        if (!"PENDING".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException("This payment has already been processed (" + payment.getStatus() + ")");
        }
        Payment saved = approved ? paymentService.markAsCompleted(id) : paymentService.markAsFailed(id);
        return listPayments(null, null, null).stream()
                .filter(v -> v.paymentId().equals(saved.getPaymentId()))
                .findFirst()
                .orElseThrow();
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    AdminOrderView toView(Order o, int itemCount, List<Payment> payments) {
        Student s = o.getStudent();
        return new AdminOrderView(o.getOrderId(), s == null ? null : s.getStudentId(),
                s == null ? "" : s.getFirstName() + " " + s.getLastName(), s == null ? "" : s.getEmail(),
                o.getOrderDate(), o.getTotalAmount(), o.getStatus().name(), itemCount, paymentSummary(payments));
    }

    private static String paymentSummary(List<Payment> payments) {
        if (payments.stream().anyMatch(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()))) return "PAID";
        if (payments.stream().anyMatch(p -> "PENDING".equalsIgnoreCase(p.getStatus()))) return "AWAITING_VERIFICATION";
        if (payments.stream().anyMatch(p -> "FAILED".equalsIgnoreCase(p.getStatus()))) return "FAILED";
        return "NONE";
    }

    private AdminPaymentView toPaymentView(Payment p, Map<Long, Booking> bookings, Map<Long, Order> orders) {
        Student student = null;
        String paidFor = "Other";
        if (p.getBookingId() != null) {
            Booking b = bookings.containsKey(p.getBookingId()) ? bookings.get(p.getBookingId())
                    : bookingRepository.findById(p.getBookingId()).orElse(null);
            student = b == null ? null : b.getStudent();
            paidFor = "Booking #" + p.getBookingId();
        } else if (p.getOrderId() != null) {
            Order o = orders.containsKey(p.getOrderId()) ? orders.get(p.getOrderId())
                    : orderRepository.findById(p.getOrderId()).orElse(null);
            student = o == null ? null : o.getStudent();
            paidFor = "Order #" + p.getOrderId();
        }
        return new AdminPaymentView(p.getPaymentId(), p.getAmount(), p.getPaymentDate(), p.getPaymentMethod(),
                p.getStatus(), p.getTransactionRef(), p.getPayerName(), p.getPayerBank(),
                p.getBookingId(), p.getOrderId(), paidFor,
                student == null ? null : student.getStudentId(),
                student == null ? "" : student.getFirstName() + " " + student.getLastName(),
                student == null ? "" : student.getEmail());
    }

    private static boolean matchesStatus(Order o, String status) {
        if (status == null || status.isBlank()) return true;
        if ("OUTSTANDING".equalsIgnoreCase(status)) {
            return o.getStatus() == PAID || o.getStatus() == IN_PROGRESS;
        }
        return o.getStatus().name().equalsIgnoreCase(status);
    }

    private static boolean matches(Order o, String search) {
        Student s = o.getStudent();
        String haystack = "#" + o.getOrderId() + " "
                + (s == null ? "" : s.getFirstName() + " " + s.getLastName() + " " + s.getEmail());
        return haystack.toLowerCase().contains(search);
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
