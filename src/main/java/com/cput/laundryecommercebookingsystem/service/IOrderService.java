package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;

import com.cput.laundryecommercebookingsystem.dto.CheckoutRequest;

import java.util.List;
import java.util.Optional;
/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

public interface IOrderService {

    Order createOrder(Student student, List<OrderItem> orderItems, double totalAmount);

    /** Turns a cart into a PENDING order, with prices from the database, and reserves the stock. */
    Order checkout(Long studentId, List<CheckoutRequest.Item> items);

    Order placeOrder(Long orderId);

    Order cancelOrder(Long orderId);
    
    void deleteOrder(Long orderId);

    Order updateOrderStatus(Long orderId, OrderStatus newStatus);

    Optional<Order> getOrderById(Long orderId);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByStudent(Long studentId);

    List<Order> getAllOrders();

}
