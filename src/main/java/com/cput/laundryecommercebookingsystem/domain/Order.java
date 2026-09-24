package com.cput.laundryecommercebookingsystem.domain;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false, updatable = false)
    private Long orderId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student studentId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addOrderItem(OrderItem item) {
        if (item != null) {
            this.orderItems.add(item);
            item.assignOrder(this);
        }
    }

    protected Order() {
    }

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.orderDate = builder.orderDate;
        this.totalAmount = builder.totalAmount;
        this.status = builder.status;
        this.studentId = builder.studentId;

        this.orderItems = new ArrayList<>();

        for (OrderItem item : builder.orderItems) {
            addOrderItem(item);
        }
    }

    public Long getOrderId() {
        return orderId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Student getStudent() {
        return studentId;
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }

    public void placeOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Only a PENDING order can be placed. Current status: " + this.status);
        }
        this.status = OrderStatus.PAID;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null.");
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("A cancelled order cannot change status.");
        }
        this.status = newStatus;
    }

    public void cancelOrder() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("A completed order cannot be cancelled.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", orderDate=" + orderDate +
                ", totalAmount=" + totalAmount +
                ", status=" + status +
                ", studentId=" + (studentId != null ? studentId.getStudentId() : null) +
                ", itemCount=" + orderItems.size() +
                '}';
    }

    public static class Builder {
        private Long orderId;
        private LocalDateTime orderDate = LocalDateTime.now();
        private double totalAmount;
        private OrderStatus status = OrderStatus.PENDING;
        private Student studentId;
        private List<OrderItem> orderItems = new ArrayList<>();

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder orderDate(LocalDateTime orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder totalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder studentId(Student student) {
            this.studentId = student;
            return this;
        }

        public Builder orderItems(List<OrderItem> orderItems) {
            this.orderItems = new ArrayList<>(orderItems);
            return this;
        }

        public Builder addOrderItem(OrderItem item) {
            this.orderItems.add(item);
            return this;
        }

        public Order build() {
            if (studentId == null) {
                throw new IllegalStateException("Order must be associated with a Student.");
            }
            return new Order(this);
        }
    }
}