package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;

import java.util.List;
import java.util.Optional;

/*OrderItemService.java
 * Service interface for OrderItem.
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

public interface IOrderItemService {

    OrderItem createOrderItem(OrderItem orderItem);

    OrderItem updateOrderItem(OrderItem orderItem);

    void deleteOrderItem(int orderItemId);

    Optional<OrderItem> getOrderItemById(int orderItemId);

    List<OrderItem> getOrderItemsByOrder(Order order);

    List<OrderItem> getOrderItemsByProduct(Product product);

    List<OrderItem> getAllOrderItems();
}
