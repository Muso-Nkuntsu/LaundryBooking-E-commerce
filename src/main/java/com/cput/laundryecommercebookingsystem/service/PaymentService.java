package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Libolwetu Nokenke 222665963
public interface PaymentService {

    Payment processPayment(
            double amount,
            LocalDateTime paymentDate,
            String paymentMethod,
            String status,
            String transactionRef,
            Long bookingId,
            Long orderId,
            Long serviceId);

    Payment markAsCompleted(Long paymentId);
    Payment markAsFailed(Long paymentId);
    Optional<Payment> getPaymentById(Long paymentId);
    List<Payment> getPaymentsByBookingId(Long bookingId);
    List<Payment> getPaymentsByStatus(String status);
    List<Payment> getAllPayments();
}

