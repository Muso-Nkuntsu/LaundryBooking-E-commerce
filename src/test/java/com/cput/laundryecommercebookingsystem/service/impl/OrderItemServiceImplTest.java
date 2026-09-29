package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.repository.IOrderItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OrderItemServiceImplTest.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

public class OrderItemServiceImplTest {

    @Mock
    private IOrderItemRepository IOrderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem orderItem;
    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {

        order = mock(Order.class);
        product = mock(Product.class);

        orderItem = new OrderItem.Builder()
                .setOrder(order)
                .setProduct(product)
                .setQuantity(2)
                .setUnitPrice(150.00)
                .build();
    }

    @Test
    void createOrderItem_returnsSavedOrderItem() {

        when(IOrderItemRepository.save(any(OrderItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderItem result = orderItemService.createOrderItem(orderItem);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(IOrderItemRepository).save(orderItem);
    }

    @Test
    void getOrderItemById_returnsOrderItem() {

        when(IOrderItemRepository.findById(1))
                .thenReturn(Optional.of(orderItem));

        Optional<OrderItem> result = orderItemService.getOrderItemById(1);

        assertTrue(result.isPresent());
    }

    @Test
    void getOrderItemsByOrder_returnsList() {

        when(IOrderItemRepository.findByOrder(order))
                .thenReturn(List.of(orderItem));

        List<OrderItem> result = orderItemService.getOrderItemsByOrder(order);

        assertEquals(1, result.size());
    }

    @Test
    void getOrderItemsByProduct_returnsList() {

        when(IOrderItemRepository.findByProduct(product))
                .thenReturn(List.of(orderItem));

        List<OrderItem> result = orderItemService.getOrderItemsByProduct(product);

        assertEquals(1, result.size());
    }

    @Test
    void getAllOrderItems_returnsList() {

        when(IOrderItemRepository.findAll())
                .thenReturn(List.of(orderItem));

        List<OrderItem> result = orderItemService.getAllOrderItems();

        assertEquals(1, result.size());
    }

    @Test
    void deleteOrderItem_callsRepository() {

        orderItemService.deleteOrderItem(1);

        verify(IOrderItemRepository).deleteById(1);
    }
}
