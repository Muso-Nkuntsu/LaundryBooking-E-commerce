package com.cput.laundryecommercebookingsystem.domain;

import jakarta.persistence.*;
import java.util.Objects;

/*OrderItem Entity
 * Represents a product contained within a customer's order.
 * Author: Sabotseng Ndaba(230235875)
 * Date: 25 July 2026
 */

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double unitPrice;

    @Column(nullable = false)
    private double subtotal;


    protected OrderItem() {
    }


    private OrderItem(Builder builder) {
        this.orderItemId = builder.orderItemId;
        this.order = builder.order;
        this.product = builder.product;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
        calculateSubtotal();
    }

    /*Calculates the subtotal for the order item.
     */

    public void calculateSubtotal() {
        this.subtotal = this.quantity * this.unitPrice;
    }
    public Long getOrderItemId() {return orderItemId;}
    public Order getOrder() {
        return order;
    }
    public Product getProduct() {
        return product;
    }
    public int getQuantity() {
        return quantity;
    }
    public double getUnitPrice() {
        return unitPrice;
    }
    public double getSubtotal() {
        return subtotal;
    }

    /*Assigns an Order to this OrderItem*/

    void assignOrder(Order order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem)) return false;
        OrderItem orderItem = (OrderItem) o;
        return orderItemId == orderItem.orderItemId &&
                quantity == orderItem.quantity &&
                Double.compare(orderItem.unitPrice, unitPrice) == 0 &&
                Double.compare(orderItem.subtotal, subtotal) == 0 &&
                Objects.equals(order, orderItem.order) &&
                Objects.equals(product, orderItem.product);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderItemId, order, product, quantity, unitPrice, subtotal);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", order=" + order +
                ", product=" + product +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }

    /*Builder class for creating OrderItem objects.
     */
    public static class Builder {

        private Long orderItemId;
        private Order order;
        private Product product;
        private int quantity;
        private double unitPrice;

        public Builder setOrderItemId(Long orderItemId) {
            this.orderItemId = orderItemId;
            return this;
        }
        public Builder setOrder(Order order) {
            this.order = order;
            return this;
        }
        public Builder setProduct(Product product) {
            this.product = product;
            return this;
        }
        public Builder setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        public Builder setUnitPrice(double unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder copy(OrderItem orderItem) {
            this.orderItemId = orderItem.orderItemId;
            this.order = orderItem.order;
            this.product = orderItem.product;
            this.quantity = orderItem.quantity;
            this.unitPrice = orderItem.unitPrice;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);

        }
    }
}
