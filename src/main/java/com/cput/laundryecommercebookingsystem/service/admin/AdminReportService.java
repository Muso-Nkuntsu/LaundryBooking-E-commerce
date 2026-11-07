package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.Role;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;

import static com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.LOW_STOCK_LEVEL;
import com.cput.laundryecommercebookingsystem.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/*AdminReportService.java
 * Every number on the admin Dashboard and Reports pages is counted here, from the database,
 * at the moment the page asks for it.
 */
@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final IStudentRepository studentRepository;
    private final iBookingRepository bookingRepository;
    private final IOrderRepository orderRepository;
    private final IOrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final IProductRepository productRepository;
    private final ILaundryMachineRepository machineRepository;
    private final AdminOrderService orderService;

    public AdminReportService(IStudentRepository studentRepository, iBookingRepository bookingRepository,
                              IOrderRepository orderRepository, IOrderItemRepository orderItemRepository,
                              PaymentRepository paymentRepository, IProductRepository productRepository,
                              ILaundryMachineRepository machineRepository, AdminOrderService orderService) {
        this.studentRepository = studentRepository;
        this.bookingRepository = bookingRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.machineRepository = machineRepository;
        this.orderService = orderService;
    }

    // =====================================================================
    // Dashboard
    // =====================================================================

    public DashboardView dashboard() {
        List<Student> students = studentRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Payment> payments = paymentRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<LaundryMachine> machines = machineRepository.findAll();
        LocalDate today = LocalDate.now();

        List<Payment> toVerify = payments.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getStatus())).toList();
        List<Product> active = products.stream().filter(Product::isActive).toList();

        List<AdminBookingView> recentBookings = bookings.stream()
                .sorted(Comparator.comparing(Booking::getId).reversed())
                .limit(6)
                .map(AdminBookingView::from)
                .toList();

        return new DashboardView(
                students.size(),
                students.stream().filter(s -> !s.isDisabled()).count(),
                students.stream().filter(s -> s.getRole() == Role.ADMIN).count(),
                students.stream().filter(s -> !s.isEmailVerified()).count(),
                bookings.size(),
                countBookings(bookings, BookingStatus.PENDING),
                countBookings(bookings, BookingStatus.CONFIRMED),
                countBookings(bookings, BookingStatus.COMPLETED),
                countBookings(bookings, BookingStatus.CANCELLED),
                bookings.stream()
                        .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                        .filter(b -> b.getTimeSlot() != null && today.equals(b.getTimeSlot().getDate()))
                        .count(),
                orders.size(),
                countOrders(orders, OrderStatus.PENDING),
                orders.stream().filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.IN_PROGRESS).count(),
                countOrders(orders, OrderStatus.COMPLETED),
                toVerify.size(),
                round(toVerify.stream().mapToDouble(Payment::getAmount).sum()),
                active.stream().filter(p -> p.getInventoryQuantity() <= LOW_STOCK_LEVEL).count(),
                active.stream().filter(p -> p.getInventoryQuantity() == 0).count(),
                machines.stream().filter(m -> m.getStatus() != MachineStatus.OUT_OF_ORDER).count(),
                machines.stream().filter(m -> m.getStatus() == MachineStatus.OUT_OF_ORDER).count(),
                machines.size(),
                recentBookings,
                orderService.recentOrders(6));
    }

    // =====================================================================
    // Reports
    // =====================================================================

    /** Everything in the last `days` days (today included). Stock is always "right now". */
    public ReportView report(int requestedDays) {
        int days = Math.min(Math.max(requestedDays, 1), 365);
        LocalDate firstDay = LocalDate.now().minusDays(days - 1L);
        LocalDateTime since = firstDay.atStartOfDay();

        // ---- bookings (by the day they were made) ----
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getBookingDate() != null && !b.getBookingDate().isBefore(since))
                .toList();
        long cancelled = countBookings(bookings, BookingStatus.CANCELLED);
        List<CountItem> bookingsByStatus = countBy(bookings, b -> b.getStatus().name());
        List<CountItem> bookingsByMachine = countBy(bookings, b -> b.getLaundryMachine() == null
                ? "Unknown" : b.getLaundryMachine().getMachineNumber());
        Map<LocalDate, Long> bookingDays = bookings.stream()
                .collect(Collectors.groupingBy(b -> b.getBookingDate().toLocalDate(), Collectors.counting()));

        // ---- orders ----
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getOrderDate() != null && !o.getOrderDate().isBefore(since))
                .toList();
        long ordersCancelled = countOrders(orders, OrderStatus.CANCELLED);
        Map<LocalDate, Long> orderDays = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getOrderDate().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Double> orderAmounts = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(o -> o.getOrderDate().toLocalDate(),
                        Collectors.summingDouble(Order::getTotalAmount)));
        double ordersValue = orders.stream().filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalAmount).sum();
        long ordersCounted = orders.size() - ordersCancelled;

        // ---- money actually received (completed payments, for bookings and orders) ----
        List<Payment> received = paymentRepository.findAll().stream()
                .filter(p -> "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .filter(p -> p.getPaymentDate() != null && !p.getPaymentDate().isBefore(since))
                .toList();
        Map<LocalDate, Long> paymentCounts = received.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Double> paymentAmounts = received.stream()
                .collect(Collectors.groupingBy(p -> p.getPaymentDate().toLocalDate(),
                        Collectors.summingDouble(Payment::getAmount)));

        // ---- top products (not counting cancelled orders) ----
        Set<Long> orderIds = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getOrderId)
                .collect(Collectors.toSet());
        List<TopProduct> topProducts = orderItemRepository.findAll().stream()
                .filter(i -> i.getOrder() != null && orderIds.contains(i.getOrder().getOrderId()) && i.getProduct() != null)
                .collect(Collectors.groupingBy(i -> i.getProduct().getName(), LinkedHashMap::new, Collectors.toList()))
                .entrySet().stream()
                .map(e -> new TopProduct(e.getKey(),
                        e.getValue().stream().mapToLong(OrderItem::getQuantity).sum(),
                        round(e.getValue().stream().mapToDouble(OrderItem::getSubtotal).sum())))
                .sorted(Comparator.comparingLong(TopProduct::quantity).reversed())
                .limit(5)
                .toList();

        // ---- stock right now ----
        Map<Long, Long> sold = orderItemRepository.findAll().stream()
                .filter(i -> i.getProduct() != null)
                .collect(Collectors.groupingBy(i -> i.getProduct().getProductId(),
                        Collectors.summingLong(OrderItem::getQuantity)));
        List<Product> products = productRepository.findAll();
        List<Product> active = products.stream().filter(Product::isActive).toList();
        List<AdminProductView> stock = products.stream()
                .sorted(Comparator.comparing(Product::isActive).reversed()
                        .thenComparingInt(Product::getInventoryQuantity)
                        .thenComparing(Product::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(p -> AdminProductView.from(p, sold.getOrDefault(p.getProductId(), 0L)))
                .toList();

        long newStudents = studentRepository.findAll().stream()
                .filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(since))
                .count();

        return new ReportView(
                days,
                bookings.size(), cancelled,
                bookings.isEmpty() ? 0 : Math.round(cancelled * 1000.0 / bookings.size()) / 10.0,
                bookingsByStatus, bookingsByMachine, perDay(firstDay, days, bookingDays, Map.of()),
                orders.size(), ordersCancelled,
                round(received.stream().mapToDouble(Payment::getAmount).sum()),
                ordersCounted == 0 ? 0 : round(ordersValue / ordersCounted),
                countBy(orders, o -> o.getStatus().name()),
                perDay(firstDay, days, orderDays, orderAmounts),
                perDay(firstDay, days, paymentCounts, paymentAmounts),
                topProducts,
                active.size(),
                active.stream().filter(p -> p.getInventoryQuantity() <= LOW_STOCK_LEVEL).count(),
                active.stream().filter(p -> p.getInventoryQuantity() == 0).count(),
                round(active.stream().mapToDouble(p -> p.getPrice() * p.getInventoryQuantity()).sum()),
                stock,
                newStudents);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private static long countBookings(List<Booking> bookings, BookingStatus status) {
        return bookings.stream().filter(b -> b.getStatus() == status).count();
    }

    private static long countOrders(List<Order> orders, OrderStatus status) {
        return orders.stream().filter(o -> o.getStatus() == status).count();
    }

    /** Groups and counts, biggest first. */
    private static <T> List<CountItem> countBy(List<T> items, Function<T, String> label) {
        return items.stream()
                .collect(Collectors.groupingBy(label, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new CountItem(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CountItem::count).reversed())
                .toList();
    }

    /** One point for every day in the period, including days with nothing, so charts have no gaps. */
    private static List<DayPoint> perDay(LocalDate firstDay, int days, Map<LocalDate, Long> counts,
                                         Map<LocalDate, Double> amounts) {
        List<DayPoint> points = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = firstDay.plusDays(i);
            points.add(new DayPoint(day, counts.getOrDefault(day, 0L), round(amounts.getOrDefault(day, 0.0))));
        }
        return points;
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
