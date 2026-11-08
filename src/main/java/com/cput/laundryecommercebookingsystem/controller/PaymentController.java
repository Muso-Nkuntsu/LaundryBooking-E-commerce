package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import com.cput.laundryecommercebookingsystem.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

//Libolwetu Nokenke 222665963
// Security: a student can only pay for, and see payments of, their own bookings and orders.
// /all, /status, /complete and /fail are admin-only (see SecurityConfig).

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final IBookingService bookingService;
    private final IOrderService orderService;
    private final CurrentStudent currentStudent;

    public PaymentController(PaymentService paymentService,
                             IBookingService bookingService,
                             IOrderService orderService,
                             CurrentStudent currentStudent) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.orderService = orderService;
        this.currentStudent = currentStudent;
    }

    /**
     * Example (card): POST /payment/create?paymentMethod=CARD&bookingId=12
     * Example (EFT):  POST /payment/create?paymentMethod=EFT&bookingId=12&transactionRef=SPIN-B12
     *                      &payerName=N%20Tukwayo&payerBank=FNB
     * The amount charged is always the booking's or order's own total; no amount is accepted from the browser.
     */
    @PostMapping("/create")
    public ResponseEntity<Payment> processPayment(@RequestParam String paymentMethod,
                                                  @RequestParam(required = false) String transactionRef,
                                                  @RequestParam(required = false) Long bookingId,
                                                  @RequestParam(required = false) Long orderId,
                                                  @RequestParam(required = false) Long serviceId,
                                                  @RequestParam(required = false) String payerName,
                                                  @RequestParam(required = false) String payerBank) {
        checkBookingOrOrderOwner(bookingId, orderId);

        Payment payment = paymentService.recordPayment(
                null, paymentMethod, transactionRef, bookingId, orderId, serviceId, payerName, payerBank);

        return ResponseEntity.ok(payment);
    }

    /** Admin only. */
    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.getPaymentById(id)
                .orElseThrow(() -> new NoSuchElementException("Payment not found with id: " + id));
        checkBookingOrOrderOwner(payment.getBookingId(), payment.getOrderId());
        return ResponseEntity.ok(payment);
    }

    /** Admin only. */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(@PathVariable Long bookingId) {
        checkBookingOrOrderOwner(bookingId, null);
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrderId(@PathVariable Long orderId) {
        checkBookingOrOrderOwner(null, orderId);
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    /** Admin only: the money is in the bank, so confirm the EFT. */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Payment> markAsCompleted(@PathVariable Long id) {
        try {
            Payment payment = paymentService.markAsCompleted(id);
            return ResponseEntity.ok(payment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Admin only. */
    @PutMapping("/{id}/fail")
    public ResponseEntity<Payment> markAsFailed(@PathVariable Long id) {

        try {
            Payment payment = paymentService.markAsFailed(id);
            return ResponseEntity.ok(payment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** 403 unless the booking or order belongs to the logged-in student (admins may see all). */
    private void checkBookingOrOrderOwner(Long bookingId, Long orderId) {
        if (bookingId != null) {
            currentStudent.checkOwner(bookingService.findById(bookingId)
                    .orElseThrow(() -> new NoSuchElementException("Booking not found with id: " + bookingId))
                    .getStudent());
        }
        if (orderId != null) {
            currentStudent.checkOwner(orderService.getOrderById(orderId)
                    .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId))
                    .getStudent());
        }
        if (bookingId == null && orderId == null) {
            currentStudent.checkOwner((Long) null); // only an admin may see a payment with no booking or order
        }
    }
}
