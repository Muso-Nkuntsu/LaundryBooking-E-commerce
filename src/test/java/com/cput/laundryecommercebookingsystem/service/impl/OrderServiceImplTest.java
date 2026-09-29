package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private IOrderRepository orderRepository;

    private OrderServiceImpl orderService;
    private Student student;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository);
        student = new Student(1);
        orderItems = List.of(new OrderItem(1));
    }

    private Order buildOrder(int orderId, OrderStatus status) {
        return new Order.Builder()
                .orderId(orderId)
                .studentId(student)
                .orderDate(LocalDateTime.now())
                .totalAmount(50.0)
                .status(status)
                .addOrderItem(new OrderItem(1))
                .build();
    }

    @Test
    void createOrder_delegatesToFactoryAndSaves() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(student, orderItems, 75.0);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(captor.capture());

        assertEquals(student, result.getStudent());
        assertEquals(75.0, result.getTotalAmount());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(captor.getValue(), result);
    }

    @Test
    void placeOrder_withExistingPendingOrder_transitionsToPaid() {
        Order pendingOrder = buildOrder(1, OrderStatus.PENDING);
        when(orderRepository.findById(1)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder(1);

        assertEquals(OrderStatus.PAID, result.getStatus());
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void placeOrder_withNonExistentId_throwsNoSuchElementException() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> orderService.placeOrder(999));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_withAlreadyPaidOrder_throwsIllegalStateException() {
        Order paidOrder = buildOrder(2, OrderStatus.PAID);
        when(orderRepository.findById(2)).thenReturn(Optional.of(paidOrder));

        assertThrows(IllegalStateException.class, () -> orderService.placeOrder(2));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_withExistingOrder_transitionsToCancelled() {
        Order order = buildOrder(3, OrderStatus.PENDING);
        when(orderRepository.findById(3)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder(3);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelOrder_withCompletedOrder_throwsIllegalStateException() {
        Order completedOrder = buildOrder(4, OrderStatus.COMPLETED);
        when(orderRepository.findById(4)).thenReturn(Optional.of(completedOrder));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(4));
    }

    @Test
    void updateOrderStatus_withValidTransition_updatesStatus() {
        Order order = buildOrder(5, OrderStatus.PAID);
        when(orderRepository.findById(5)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateOrderStatus(5, OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
    }

    @Test
    void updateOrderStatus_withNonExistentId_throwsNoSuchElementException() {
        when(orderRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> orderService.updateOrderStatus(999, OrderStatus.PAID));
    }

    @Test
    void getOrderById_delegatesToRepository() {
        Order order = buildOrder(6, OrderStatus.PENDING);
        when(orderRepository.findById(6)).thenReturn(Optional.of(order));

        Optional<Order> result = orderService.getOrderById(6);

        assertEquals(order, result.orElseThrow());
    }

    @Test
    void getOrdersByStatus_delegatesToRepository() {
        List<Order> pendingOrders = List.of(buildOrder(7, OrderStatus.PENDING));
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(pendingOrders);

        List<Order> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertEquals(pendingOrders, result);
    }

    @Test
    void getAllOrders_delegatesToRepository() {
        List<Order> allOrders = List.of(buildOrder(8, OrderStatus.PENDING), buildOrder(9, OrderStatus.PAID));
        when(orderRepository.findAll()).thenReturn(allOrders);

        List<Order> result = orderService.getAllOrders();

        assertEquals(allOrders, result);
    }
}
