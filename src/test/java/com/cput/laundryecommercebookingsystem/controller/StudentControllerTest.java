/*StudentControllerTest.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 29 July 2026
 */


package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.factory.StudentFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String baseUrl = "/student";

    private Student student;

    @BeforeEach
    void setUp() {

        student = StudentFactory.createStudent(
                "Katlego",
                "Ndaba",
                "katlegondaba08@gmail.com",
                "0625067447",
                "Ndaba447"
        );
    }

    @Test
    @Order(1)
    void testCreate() {

        ResponseEntity<Student> response =
                restTemplate.postForEntity(baseUrl + "/create", student, Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(student.getEmail(), response.getBody().getEmail());
    }

    @Test
    @Order(2)
    void testRead() {

        Student created =
                restTemplate.postForObject(baseUrl + "/create", student, Student.class);

        assertNotNull(created);

        ResponseEntity<Student> response =
                restTemplate.getForEntity(baseUrl + "/read/" + created.getStudentId(), Student.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(3)
    void testGetAll() {

        ResponseEntity<Student[]> response =
                restTemplate.getForEntity(baseUrl + "/getall", Student[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
