/**
 * LaundryServiceControllerTest.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.factory.LaundryServiceFactory;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LaundryServiceControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "/laundry-service";
    private LaundryService laundryService;

    @BeforeEach
    void setUp() {
        laundryService = LaundryServiceFactory.createLaundryService("Ironing Only", "Pressing and ironing per item", 25.00);
    }

    @Test
    @Order(1)
    void testCreate() {
        ResponseEntity<LaundryService> response = restTemplate.postForEntity(baseUrl + "/create", laundryService, LaundryService.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(laundryService.getId(), response.getBody().getId());
    }

    @Test
    @Order(2)
    void testRead() {
        LaundryService created = restTemplate.postForObject(baseUrl + "/create", laundryService, LaundryService.class);
        ResponseEntity<LaundryService> response = restTemplate.getForEntity(baseUrl + "/read/" + created.getId(), LaundryService.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(3)
    void testGetAll() {
        ResponseEntity<LaundryService[]> response = restTemplate.getForEntity(baseUrl + "/getall", LaundryService[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }
}

