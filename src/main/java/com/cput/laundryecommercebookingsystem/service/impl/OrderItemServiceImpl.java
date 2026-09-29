package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.repository.IOrderItemRepository;
import com.cput.laundryecommercebookingsystem.service.IOrderItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/*OrderItemServiceImpl.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

@Service
public class OrderItemServiceImpl implements IOrderItemService {

    private final IOrderItemRepository IOrderItemRepository;

    public OrderItemServiceImpl(IOrderItemRepository IOrderItemRepository) {
        this.IOrderItemRepository = IOrderItemRepository;
    }

    @Override
    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) {
        return IOrderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public OrderItem updateOrderItem(OrderItem orderItem) {
        if (!IOrderItemRepository.existsById(orderItem.getOrderItemId())) {
            throw new NoSuchElementException("OrderItem not found.");
        }
        return IOrderItemRepository.save(orderItem);
    }

    @Override
    @Transactional
    public void deleteOrderItem(int orderItemId) {
        IOrderItemRepository.deleteById(orderItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrderItem> getOrderItemById(int orderItemId) {
        return IOrderItemRepository.findById(orderItemId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrder(Order order) {
        return IOrderItemRepository.findByOrder(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByProduct(Product product) {
        return IOrderItemRepository.findByProduct(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItem> getAllOrderItems() {
        return IOrderItemRepository.findAll();
    }
}
