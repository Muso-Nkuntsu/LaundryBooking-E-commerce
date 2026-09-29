/**
 * ILaundryServiceRepository.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;

import java.util.List;

/**
 * Spring Data JPA Repository interface for LaundryService entity persistence operations.
 */
@Repository
public interface ILaundryServiceRepository extends JpaRepository<LaundryService, String> {

    List<LaundryService> findByServiceNameContainingIgnoreCase(String serviceName);
}
