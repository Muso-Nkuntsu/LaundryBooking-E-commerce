package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Payment;
import com.cput.laundryecommercebookingsystem.factory.PaymentFactory;
import com.cput.laundryecommercebookingsystem.repository.PaymentRepository;
import com.cput.laundryecommercebookingsystem.service.ILaundryService;
import com.cput.laundryecommercebookingsystem.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// Libolwetu Nokenke 222665963

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ILaundryService laundryService;

    public PaymentServiceImpl(PaymentRepository paymentRepository,ILaundryService laundryService) {
        this.paymentRepository = paymentRepository;
        this.laundryService = laundryService;
    }
    @Override
    @Transactional
    public Payment processPayment(
            double amount,
            LocalDateTime paymentDate,
            String paymentMethod,
            String status,
            String transactionRef,
            Long bookingId,
            Long orderId,
            Long serviceId) {

        LaundryService service = null;

        if(serviceId != null){
            service = laundryService.read(serviceId);

            if (service == null) {
                throw new NoSuchElementException("Laundry service not found with id: " + serviceId);
            }
        }
        Payment payment = PaymentFactory.createPayment(
                amount,
                paymentDate,
                paymentMethod,
                status,
                transactionRef,
                bookingId,
                orderId,
                service);
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment markAsCompleted(Long paymentId) {
        Payment existing = getPaymentOrThrow(paymentId);
        Payment updated = rebuildWithStatus(existing, "Completed");
        return paymentRepository.save(updated);
    }
    @Override
    @Transactional
    public Payment markAsFailed(Long paymentId) {
        Payment existing = getPaymentOrThrow(paymentId);
        Payment updated = rebuildWithStatus(existing, "Failed");
        return paymentRepository.save(updated);
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
    public List<Payment> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
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
                .build();
    }
}

