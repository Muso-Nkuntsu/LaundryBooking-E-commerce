package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.factory.OrderFactory;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

@Service
public class OrderServiceImpl implements IOrderService { private final IOrderRepository IOrderRepository;

    public OrderServiceImpl(IOrderRepository IOrderRepository) {
        this.IOrderRepository = IOrderRepository;
    }

    @Override
    @Transactional
    public Order createOrder(Student student, List<OrderItem> orderItems, double totalAmount) {
        Order order = OrderFactory.createOrder(student, orderItems, totalAmount);
        return IOrderRepository.save(order);
    }

    @Override
    @Transactional
    public Order placeOrder(int orderId) {
        Order order = getOrderOrThrow(orderId);
        order.placeOrder();
        return IOrderRepository.save(order);
    }

    @Override
    @Transactional
    public Order cancelOrder(int orderId) {
        Order order = getOrderOrThrow(orderId);
        order.cancelOrder();
        return IOrderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrderStatus(int orderId, OrderStatus newStatus) {
        Order order = getOrderOrThrow(orderId);
        order.updateStatus(newStatus);
        return IOrderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(int orderId) {
        return IOrderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return IOrderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStudent(int studentId) {
        return IOrderRepository.findByStudentPrimaryKey(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return IOrderRepository.findAll();
    }

    private Order getOrderOrThrow(int orderId) {
        return IOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
    }

}
