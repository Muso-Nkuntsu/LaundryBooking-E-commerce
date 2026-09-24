package com.cput.laundryecommercebookingsystem.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

// 222665963 Libolwetu Nokenke

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "status")
    private String status;

    @Column(name = "transaction_ref")
    private String transactionRef;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "service_id", nullable = false)
    private LaundryService service;

    protected Payment() {}


    private Payment(Builder builder) {
        this.paymentId = builder.paymentId;
        this.amount = builder.amount;
        this.paymentDate = builder.paymentDate;
        this.paymentMethod = builder.paymentMethod;
        this.status = builder.status;
        this.transactionRef = builder.transactionRef;
        this.bookingId = builder.bookingId;
        this.orderId = builder.orderId;
        this.service = builder.service;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getPaymentId() {
        return paymentId;
    }
    public double getAmount() {
        return amount;
    }
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    public String getPaymentMethod() {
        return paymentMethod;
    }
    public String getStatus() {
        return status;
    }
    public String getTransactionRef() {
        return transactionRef;
    }
    public Long getBookingId() {
        return bookingId;
    }
    public Long getOrderId() {
        return orderId;
    }
    public LaundryService getService() {
        return service;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Double.compare(payment.amount, amount) == 0 &&
                Objects.equals(paymentId, payment.paymentId) &&
                Objects.equals(bookingId, payment.bookingId) &&
                Objects.equals(orderId, payment.orderId) &&
                Objects.equals(service, payment.service) &&
                Objects.equals(paymentDate, payment.paymentDate) &&
                Objects.equals(paymentMethod, payment.paymentMethod) &&
                Objects.equals(status, payment.status) &&
                Objects.equals(transactionRef, payment.transactionRef);
    }
    @Override
    public int hashCode() {
        return Objects.hash(paymentId, amount, paymentDate, paymentMethod,
                status, transactionRef, bookingId, orderId, service);
    }
    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", amount=" + amount +
                ", paymentDate=" + paymentDate +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", status='" + status + '\'' +
                ", transactionRef='" + transactionRef + '\'' +
                ", bookingId=" + bookingId +
                ", orderId=" + orderId +
                ", service=" + service +
                '}';
    }
    public static class Builder {
        private Long paymentId;
        private double amount;
        private LocalDateTime paymentDate;
        private String paymentMethod;
        private String status;
        private String transactionRef;
        private Long bookingId;
        private Long orderId;
        private LaundryService service;

        public Builder setPaymentId(Long paymentId) {
            this.paymentId = paymentId;
            return this;
        }
        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }
        public Builder setPaymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }
        public Builder setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }
        public Builder setTransactionRef(String transactionRef) {
            this.transactionRef = transactionRef;
            return this;
        }
        public Builder setBookingId(Long bookingId) {
            this.bookingId = bookingId;
            return this;
        }
        public Builder setOrderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }
        public Builder setService(LaundryService service) {
            this.service = service;
            return this;
        }
        public Payment build() {
            return new Payment(this);
        }
    }
}
