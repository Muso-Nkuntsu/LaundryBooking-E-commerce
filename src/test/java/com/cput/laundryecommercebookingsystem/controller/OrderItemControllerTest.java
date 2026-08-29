package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.service.IOrderItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * OrderItemControllerTest.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 29 August 2026
 */

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderItemControllerTest {

    private IOrderItemService orderItemService;
    private OrderItemController orderItemController;

    private OrderItem orderItem;

    @BeforeEach
    void setUp() {

        orderItemService = mock(IOrderItemService.class);
        orderItemController = new OrderItemController(orderItemService);

        com.cput.laundryecommercebookingsystem.domain.Order order =
                mock(com.cput.laundryecommercebookingsystem.domain.Order.class);

        Product product = mock(Product.class);

        orderItem = new OrderItem.Builder()
                .setOrder(order)
                .setProduct(product)
                .setQuantity(2)
                .setUnitPrice(150.00)
                .build();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testCreate() {

        when(orderItemService.createOrderItem(orderItem))
                .thenReturn(orderItem);

        ResponseEntity<OrderItem> response =
                orderItemController.create(orderItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getQuantity());

        verify(orderItemService).createOrderItem(orderItem);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testRead() {

        when(orderItemService.getOrderItemById(1))
                .thenReturn(Optional.of(orderItem));

        ResponseEntity<OrderItem> response =
                orderItemController.read(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(orderItemService).getOrderItemById(1);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void testUpdate() {

        when(orderItemService.updateOrderItem(orderItem))
                .thenReturn(orderItem);

        ResponseEntity<OrderItem> response =
                orderItemController.update(orderItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        verify(orderItemService).updateOrderItem(orderItem);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void testDelete() {

        ResponseEntity<Boolean> response =
                orderItemController.delete(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody());

        verify(orderItemService).deleteOrderItem(1);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void testGetAll() {

        when(orderItemService.getAllOrderItems())
                .thenReturn(List.of(orderItem));

        ResponseEntity<List<OrderItem>> response =
                orderItemController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(orderItemService).getAllOrderItems();
    }
}