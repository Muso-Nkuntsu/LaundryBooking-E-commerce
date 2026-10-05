package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

//Libolwetu Nokenke 222665963

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PostMapping("/create")
    public ResponseEntity<Payment> processPayment(@RequestParam double amount,
                                                  @RequestParam String paymentMethod,
                                                  @RequestParam(defaultValue = "PENDING") String status,
                                                  @RequestParam(required = false) String transactionRef,
                                                  @RequestParam(required = false) Long bookingId,
                                                  @RequestParam(required = false) Long orderId,
                                                  @RequestParam(required = false) Long serviceId) {

        Payment payment = paymentService.processPayment(
                amount,
                LocalDateTime.now(),
                paymentMethod,
                status,
                transactionRef,
                bookingId,
                orderId,
                serviceId
        );

        return ResponseEntity.ok(payment);
    }
    @GetMapping("/all")
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {

        Optional<Payment> payment = paymentService.getPaymentById(id);

        if (payment.isPresent()) {
            return ResponseEntity.ok(payment.get());
        }

        return ResponseEntity.notFound().build();
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Payment>> getPaymentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<Payment>> getPaymentsByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentsByBookingId(bookingId));
    }
    @PutMapping("/{id}/complete")
    public ResponseEntity<Payment> markAsCompleted(@PathVariable Long id) {
        try {
            Payment payment = paymentService.markAsCompleted(id);
            return ResponseEntity.ok(payment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/fail")
    public ResponseEntity<Payment> markAsFailed(@PathVariable Long id) {

        try {
            Payment payment = paymentService.markAsFailed(id);
            return ResponseEntity.ok(payment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}