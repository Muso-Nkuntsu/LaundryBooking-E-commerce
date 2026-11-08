package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.dto.CheckoutRequest;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Talent Nocuze
 * 25 July 2026
 * Security: a student only sees and cancels their own orders.
 * Listing all orders, /place (marks PAID) and delete are admin-only (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final IOrderService orderService;
    private final CurrentStudent currentStudent;

    public OrderController(IOrderService orderService, CurrentStudent currentStudent) {
        this.orderService = orderService;
        this.currentStudent = currentStudent;
    }

    /** Turns the student's cart into a PENDING order. Body: see CheckoutRequest. */
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestBody CheckoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Send the cart as the request body");
        }
        // A student always orders for themselves, whatever studentId the browser sends.
        Long ownerId = currentStudent.resolveStudentId(request.studentId());
        Order order = orderService.checkout(ownerId, request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /** Admin only. */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = findOrder(id);
        currentStudent.checkOwner(order.getStudent());
        return ResponseEntity.ok(order);
    }

    /** Admin only. */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    // Get orders by student ID
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Order>> getOrdersByStudent(
            @PathVariable Long studentId) {
        currentStudent.checkOwner(studentId);
        return ResponseEntity.ok(orderService.getOrdersByStudent(studentId));
    }

    /** Admin only: marks an order PAID without a payment record. */
    @PatchMapping("/{id}/place")
    public ResponseEntity<Order> placeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.placeOrder(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        currentStudent.checkOwner(findOrder(id).getStudent());
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    /** Admin only. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    private Order findOrder(Long id) {
        return orderService.getOrderById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + id));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException ex) {

        return ResponseEntity
                .badRequest()
                .body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(
            IllegalStateException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }
}
