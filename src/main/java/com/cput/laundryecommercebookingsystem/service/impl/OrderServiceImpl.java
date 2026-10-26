package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import com.cput.laundryecommercebookingsystem.factory.OrderFactory;
import com.cput.laundryecommercebookingsystem.repository.IOrderRepository;
import com.cput.laundryecommercebookingsystem.service.IOrderService;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.dto.CheckoutRequest;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

@Service
public class OrderServiceImpl implements IOrderService {

    private final IOrderRepository IOrderRepository;
    private final IStudentRepository studentRepository;
    private final IProductRepository productRepository;

    public OrderServiceImpl(IOrderRepository IOrderRepository,
                            IStudentRepository studentRepository,
                            IProductRepository productRepository) {
        this.IOrderRepository = IOrderRepository;
        this.studentRepository = studentRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Order checkout(Long studentId, List<CheckoutRequest.Item> items) {
        if (studentId == null) {
            throw new IllegalArgumentException("studentId is required");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty");
        }
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));

        // The same product added twice becomes one line with the quantities added together.
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CheckoutRequest.Item item : items) {
            if (item == null || item.productId() == null) {
                throw new IllegalArgumentException("Every cart item needs a productId");
            }
            if (item.quantity() <= 0) {
                throw new IllegalArgumentException("Quantities must be at least 1");
            }
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;
        for (Map.Entry<Long, Integer> line : quantities.entrySet()) {
            Product product = productRepository.findById(line.getKey())
                    .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + line.getKey()));
            int quantity = line.getValue();
            if (product.getInventoryQuantity() < quantity) {
                throw new IllegalStateException("Only " + product.getInventoryQuantity() + " of "
                        + product.getName() + " left in stock");
            }

            orderItems.add(new OrderItem.Builder()
                    .setProduct(product)
                    .setQuantity(quantity)
                    .setUnitPrice(product.getPrice())
                    .build());
            total += product.getPrice() * quantity;

            // Reserve the stock now so two students can't buy the last item. Cancelling gives it back.
            changeStock(product, -quantity);
        }

        // The order starts PENDING; paying for it marks it PAID (see PaymentServiceImpl).
        Order order = OrderFactory.createOrder(student, orderItems, Math.round(total * 100) / 100.0);
        return IOrderRepository.save(order);
    }

    private void changeStock(Product product, int change) {
        Product updated = new Product.Builder()
                .copy(product)
                .setInventoryQuantity(product.getInventoryQuantity() + change)
                .build();
        productRepository.save(updated);
    }

    @Override
    @Transactional
    public Order createOrder(Student student, List<OrderItem> orderItems, double totalAmount) {
        Order order = OrderFactory.createOrder(student, orderItems, totalAmount);
        return IOrderRepository.save(order);
    }


    @Transactional
    public Order placeOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        order.placeOrder();
        return IOrderRepository.save(order);
    }


    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return order;
        }
        order.cancelOrder();
        // Put the reserved stock back on the shelf.
        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getProductId()).orElse(null);
            if (product != null) {
                changeStock(product, item.getQuantity());
            }
        }
        return IOrderRepository.save(order);
    }


    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderOrThrow(orderId);
        order.updateStatus(newStatus);
        return IOrderRepository.save(order);
    }


    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        return IOrderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return IOrderRepository.findByStatus(status);
    }


    @Transactional(readOnly = true)
    public List<Order> getOrdersByStudent(Long studentId) {
        return IOrderRepository.findByStudentPrimaryKey(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return IOrderRepository.findAll();
    }

    private Order getOrderOrThrow(Long orderId) {
        return IOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId){
        Order order = getOrderOrThrow(orderId);
        IOrderRepository.delete(order);
    }

}
