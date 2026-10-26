package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.factory.PaymentFactory;
import com.cput.laundryecommercebookingsystem.repository.PaymentRepository;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import com.cput.laundryecommercebookingsystem.service.ILaundryService;
import com.cput.laundryecommercebookingsystem.service.INotificationService;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import com.cput.laundryecommercebookingsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// Libolwetu Nokenke 222665963

/**
 * Payment rules:
 * - A payment for a booking or an order always charges that record's own total, never an amount from the browser.
 * - CARD: there is no payment gateway connected, so a card payment is treated as approved straight away
 *   (simulated). It is saved as COMPLETED and the booking is confirmed / the order is marked PAID.
 * - EFT: the money only arrives later, so the payment is saved as PENDING with its reference.
 *   The booking or order stays PENDING until someone checks the bank account and calls
 *   PUT /payment/{id}/complete, which confirms it.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    private final PaymentRepository paymentRepository;
    private final ILaundryService laundryService;
    private final IBookingService bookingService;
    private final IOrderService orderService;
    private final INotificationService notificationService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ILaundryService laundryService,
                              IBookingService bookingService,
                              IOrderService orderService,
                              INotificationService notificationService) {
        this.paymentRepository = paymentRepository;
        this.laundryService = laundryService;
        this.bookingService = bookingService;
        this.orderService = orderService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Payment processPayment(double amount, LocalDateTime paymentDate, String paymentMethod,
                                  String status, String transactionRef,
                                  Long bookingId, Long orderId, Long serviceId) {
        return recordPayment(amount, paymentMethod, transactionRef, bookingId, orderId, serviceId, null, null);
    }

    @Override
    @Transactional
    public Payment recordPayment(Double amount, String paymentMethod, String transactionRef,
                                 Long bookingId, Long orderId, Long serviceId,
                                 String payerName, String payerBank) {

        String method = paymentMethod == null ? "" : paymentMethod.trim().toUpperCase();
        if (!method.equals("CARD") && !method.equals("EFT")) {
            throw new IllegalArgumentException("Payment method must be CARD or EFT");
        }
        if (bookingId != null && orderId != null) {
            throw new IllegalArgumentException("A payment is for a booking or an order, not both");
        }

        double amountDue = amount != null ? amount : 0;

        if (bookingId != null) {
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + bookingId));
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                throw new IllegalStateException("This booking was cancelled, so it can't be paid for");
            }
            if (booking.getStatus() != BookingStatus.PENDING) {
                throw new IllegalStateException("This booking has already been paid for");
            }
            amountDue = booking.getTotalAmount();
        }

        if (orderId != null) {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("This order was cancelled, so it can't be paid for");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new IllegalStateException("This order has already been paid for");
            }
            amountDue = order.getTotalAmount();
        }

        // An EFT that is still waiting to be checked must not be paid a second time.
        boolean awaitingEft = (bookingId != null ? paymentRepository.findByBookingId(bookingId)
                : orderId != null ? paymentRepository.findByOrderId(orderId) : List.<Payment>of())
                .stream().anyMatch(p -> PENDING.equalsIgnoreCase(p.getStatus()));
        if (awaitingEft) {
            throw new IllegalStateException("An EFT payment for this is already waiting to be verified");
        }

        if (method.equals("EFT")) {
            if (isBlank(payerName) || isBlank(payerBank)) {
                throw new IllegalArgumentException("For EFT, give the account holder's name and the bank you paid from");
            }
            if (isBlank(transactionRef)) {
                throw new IllegalArgumentException("For EFT, give the payment reference you used");
            }
        }

        LaundryService service = null;
        if (serviceId != null) {
            service = laundryService.read(serviceId);
            if (service == null) {
                throw new NoSuchElementException("Laundry service not found with id: " + serviceId);
            }
        }

        String status = method.equals("CARD") ? COMPLETED : PENDING;
        Payment payment = PaymentFactory.createPayment(
                amountDue, LocalDateTime.now(), method, status,
                isBlank(transactionRef) ? null : transactionRef.trim(),
                bookingId, orderId, service,
                isBlank(payerName) ? null : payerName.trim(),
                isBlank(payerBank) ? null : payerBank.trim());
        Payment saved = paymentRepository.save(payment);

        if (COMPLETED.equals(status)) {
            settle(saved);
        }
        return saved;
    }

    /** Confirms an EFT once the money is in the bank. */
    @Override
    @Transactional
    public Payment markAsCompleted(Long paymentId) {
        Payment existing = getPaymentOrThrow(paymentId);
        if (COMPLETED.equalsIgnoreCase(existing.getStatus())) {
            return existing;
        }
        Payment saved = paymentRepository.save(rebuildWithStatus(existing, COMPLETED));
        settle(saved);
        return saved;
    }

    /** The EFT never arrived. The booking or order stays PENDING, so the student can pay again or cancel. */
    @Override
    @Transactional
    public Payment markAsFailed(Long paymentId) {
        Payment existing = getPaymentOrThrow(paymentId);
        if (COMPLETED.equalsIgnoreCase(existing.getStatus())) {
            throw new IllegalStateException("A completed payment can't be marked as failed");
        }
        return paymentRepository.save(rebuildWithStatus(existing, FAILED));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // ---------- Helpers ----------

    /** A completed payment confirms what it paid for and tells the student. */
    private void settle(Payment payment) {
        Student student = null;

        if (payment.getBookingId() != null) {
            Booking booking = bookingService.confirmBooking(payment.getBookingId());
            student = booking.getStudent();
        }
        if (payment.getOrderId() != null) {
            Order order = orderService.placeOrder(payment.getOrderId()); // PENDING -> PAID
            student = order.getStudent();
        }
        if (student != null) {
            notificationService.sendPaymentConfirmation(student, payment.getPaymentId());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Payment getPaymentOrThrow(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException("Payment not found with id: " + paymentId));
    }

    private Payment rebuildWithStatus(Payment existing, String newStatus) {
        return Payment.builder()
                .setPaymentId(existing.getPaymentId())
                .setAmount(existing.getAmount())
                .setPaymentDate(existing.getPaymentDate())
                .setPaymentMethod(existing.getPaymentMethod())
                .setStatus(newStatus)
                .setTransactionRef(existing.getTransactionRef())
                .setBookingId(existing.getBookingId())
                .setOrderId(existing.getOrderId())
                .setService(existing.getService())
                .setPayerName(existing.getPayerName())
                .setPayerBank(existing.getPayerBank())
                .build();
    }
}
