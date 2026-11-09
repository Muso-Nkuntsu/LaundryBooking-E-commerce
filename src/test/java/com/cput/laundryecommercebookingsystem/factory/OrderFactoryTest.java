package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */

class OrderFactoryTest {

    private Student student;
    private List<OrderItem> orderItems;

    @BeforeEach
    void setUp() {
        student = new Student(1);
        orderItems = List.of(new OrderItem(101), new OrderItem(102));
    }

    @Test
    void createOrder_withValidInputs_returnsOrderWithExpectedValues() {
        Order order = OrderFactory.createOrder(student, orderItems, 149.99);

        assertNotNull(order);
        assertEquals(student, order.getStudent());
        assertEquals(2, order.getOrderItems().size());
        assertEquals(149.99, order.getTotalAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getOrderDate());
    }

    @Test
    void createOrder_defaultOverload_usesCurrentDateAndPendingStatus() {
        LocalDateTime before = LocalDateTime.now();
        Order order = OrderFactory.createOrder(student, orderItems, 50.0);
        LocalDateTime after = LocalDateTime.now();

        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertTrue(!order.getOrderDate().isBefore(before) && !order.getOrderDate().isAfter(after));
    }

    @Test
    void createOrder_withExplicitDateAndStatus_usesProvidedValues() {
        LocalDateTime explicitDate = LocalDateTime.of(2026, 1, 15, 10, 30);

        Order order = OrderFactory.createOrder(student, orderItems, 75.0, explicitDate, OrderStatus.PAID);

        assertEquals(explicitDate, order.getOrderDate());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void createOrder_relationshipWithStudent_isCorrectlySet() {
        Order order = OrderFactory.createOrder(student, orderItems, 20.0);

        assertEquals(student.getStudentId(), order.getStudent().getStudentId());
    }
    @Test
    void createOrder_withNullStudent_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(null, orderItems, 50.0));
    }

    @Test
    void createOrder_withNullOrderItems_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(student, null, 50.0));
    }

    @Test
    void createOrder_withEmptyOrderItems_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(student, Collections.emptyList(), 50.0));
    }

    @Test
    void createOrder_withNegativeTotalAmount_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(student, orderItems, -1.0));
    }

    @Test
    void createOrder_withNullOrderDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(student, orderItems, 50.0, null, OrderStatus.PENDING));
    }

    @Test
    void createOrder_withNullStatus_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> OrderFactory.createOrder(student, orderItems, 50.0, LocalDateTime.now(), null));
    }
}
