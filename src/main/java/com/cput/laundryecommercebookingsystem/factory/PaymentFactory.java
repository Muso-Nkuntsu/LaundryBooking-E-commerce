package com.cput.laundryecommercebookingsystem.factory;

// 222665963 Libolwetu Nokenke
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Payment;

import java.time.LocalDateTime;

public class PaymentFactory {

    public static Payment createPayment(double amount, LocalDateTime paymentDate, String paymentMethod,
                                        String status, String transactionRef,
                                        Long bookingId, Long orderId, LaundryService service) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (paymentDate == null) {
            throw new IllegalArgumentException("Payment date cannot be null");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (bookingId == null && orderId == null && service == null) {
            throw new IllegalArgumentException("Payment must be linked to a booking, order, or service");
        }

        return Payment.builder()
                .setAmount(amount)
                .setPaymentDate(paymentDate)
                .setPaymentMethod(paymentMethod)
                .setStatus(status)
                .setTransactionRef(transactionRef)
                .setBookingId(bookingId)
                .setOrderId(orderId)
                .setService(service)
                .build();
    }
}
