package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Order;
import com.cput.laundryecommercebookingsystem.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

public interface IOrderRepository extends JpaRepository<Order, Integer>{

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.studentId.studentId = :studentId")
    List<Order> findByStudentPrimaryKey(@Param("studentId") int studentId);

}
