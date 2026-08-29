package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.service.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/*OrderItemController.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 29 August 2026
 */

@RestController
@RequestMapping("/order-item")
public class OrderItemController {

    private final IOrderItemService orderItemService;

    @Autowired
    public OrderItemController(IOrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @PostMapping("/create")
    public ResponseEntity<OrderItem> create(@RequestBody OrderItem orderItem) {

        OrderItem created = orderItemService.createOrderItem(orderItem);

        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<OrderItem> read(@PathVariable int id) {

        Optional<OrderItem> orderItem = orderItemService.getOrderItemById(id);

        return orderItem.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    public ResponseEntity<OrderItem> update(@RequestBody OrderItem orderItem) {

        try {
            OrderItem updated = orderItemService.updateOrderItem(orderItem);
            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable int id) {

        orderItemService.deleteOrderItem(id);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<OrderItem>> getAll() {

        return ResponseEntity.ok(orderItemService.getAllOrderItems());
    }
}