/**
 * IProductRepository.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cput.laundryecommercebookingsystem.domain.Product;

@Repository
public interface IProductRepository extends JpaRepository<Product, String> {
}
