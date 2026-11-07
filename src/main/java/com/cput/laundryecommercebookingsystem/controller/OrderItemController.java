package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.service.IOrderItemService;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/*OrderItemController.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 29 August 2026
 * Security: students only read items of their own orders. Items are created by checkout,
 * so /create, /update, /delete and /getall are admin-only (see SecurityConfig).
 */

@RestController
@RequestMapping("/order-item")
public class OrderItemController {

    private final IOrderItemService orderItemService;
    private final IOrderService orderService;
    private final CurrentStudent currentStudent;

    @Autowired
    public OrderItemController(IOrderItemService orderItemService,
                               IOrderService orderService,
                               CurrentStudent currentStudent) {
        this.orderItemService = orderItemService;
        this.orderService = orderService;
        this.currentStudent = currentStudent;
    }

    /** Admin only. */
    @PostMapping("/create")
    public ResponseEntity<OrderItem> create(@RequestBody OrderItem orderItem) {

        OrderItem created = orderItemService.createOrderItem(orderItem);

        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<OrderItem> read(@PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItemById(id)
                .orElseThrow(() -> new NoSuchElementException("Order item not found with id: " + id));
        currentStudent.checkOwner(item.getOrder().getStudent());
        return ResponseEntity.ok(item);
    }

    /** Admin only. */
    @PostMapping("/update")
    public ResponseEntity<OrderItem> update(@RequestBody OrderItem orderItem) {

        try {
            OrderItem updated = orderItemService.updateOrderItem(orderItem);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** Admin only. */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {

        orderItemService.deleteOrderItem(id);
        return ResponseEntity.ok(true);
    }

    /** Items of one order: GET /order-item/order/12 */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItem>> getByOrder(@PathVariable Long orderId) {
        currentStudent.checkOwner(orderService.getOrderById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId))
                .getStudent());
        return ResponseEntity.ok(orderItemService.getOrderItemsByOrderId(orderId));
    }

    /** Everything one student has ordered: GET /order-item/student/1 */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<OrderItem>> getByStudent(@PathVariable Long studentId) {
        currentStudent.checkOwner(studentId);
        return ResponseEntity.ok(orderItemService.getOrderItemsByStudentId(studentId));
    }

    /** Admin only. */
    @GetMapping("/getall")
    public ResponseEntity<List<OrderItem>> getAll() {

        return ResponseEntity.ok(orderItemService.getAllOrderItems());
    }
}
