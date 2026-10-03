package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Libolwetu Nokenke 222665963

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatus(String status);
    List<Payment> findByBookingId(Long bookingId);
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByServiceId(Long serviceId);
}
