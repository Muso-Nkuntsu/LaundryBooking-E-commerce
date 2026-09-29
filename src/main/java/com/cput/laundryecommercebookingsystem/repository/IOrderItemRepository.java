package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*OrderItemRepository.java
 * Author: Sabotseng Ndaba
 * Date: 28 July 2026
 */

public interface IOrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByProduct(Product product);

}
