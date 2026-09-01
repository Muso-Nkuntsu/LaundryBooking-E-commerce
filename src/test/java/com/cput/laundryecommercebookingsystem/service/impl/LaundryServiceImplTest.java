
package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.service.impl.LaundryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.factory.LaundryServiceFactory;

import static org.junit.jupiter.api.Assertions.*;
/**
 * LaundryServiceImplTest.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LaundryServiceImplTest {

    @Autowired
    private LaundryServiceImpl service;

    private LaundryService laundryService;

    @BeforeEach
    void setUp() {
        laundryService = LaundryServiceFactory.createLaundryService("Dry Cleaning", "Suit and dress dry cleaning", 150.00);
    }

    @Test
    @Order(1)
    void testCreate() {
        LaundryService created = service.create(laundryService);
        assertNotNull(created);
        assertEquals(laundryService.getId(), created.getId());
    }

    @Test
    @Order(2)
    void testRead() {
        LaundryService created = service.create(laundryService);
        LaundryService read = service.read(created.getId());
        assertNotNull(read);
        assertEquals(created.getId(), read.getId());
    }

    @Test
    @Order(3)
    void testUpdate() {
        LaundryService created = service.create(laundryService);
        LaundryService updatedService = new LaundryService.Builder()
                .copy(created)
                .setPrice(165.00)
                .build();

        LaundryService updated = service.update(updatedService);
        assertNotNull(updated);
        assertEquals(165.00, updated.getPrice());
    }

    @Test
    @Order(4)
    void testGetAll() {
        service.create(laundryService);
        assertFalse(service.getAll().isEmpty());
    }

    @Test
    @Order(5)
    void testDelete() {
        LaundryService created = service.create(laundryService);
        boolean deleted = service.delete(created.getId());
        assertTrue(deleted);
    }
}

