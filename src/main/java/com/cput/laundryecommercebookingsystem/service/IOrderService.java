package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;

import java.util.List;
import java.util.Optional;
/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

public interface IOrderService {

    Order createOrder(Student student, List<OrderItem> orderItems, double totalAmount);

    Order placeOrder(int orderId);

    Order cancelOrder(int orderId);

    Order updateOrderStatus(int orderId, OrderStatus newStatus);

    Optional<Order> getOrderById(int orderId);

    List<Order> getOrdersByStatus(OrderStatus status);

    List<Order> getOrdersByStudent(int studentId);

    List<Order> getAllOrders();

}
