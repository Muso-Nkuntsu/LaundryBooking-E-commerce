package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*StudentServiceImplTest.java
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

public class StudentServiceImplTest {

    @Mock
    private IStudentRepository IStudentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student student;

    @BeforeEach
    void setUp() {

        student = new Student.Builder()
                .setStudentId(1L)
                .setFirstName("Katlego")
                .setLastName("Ndaba")
                .setEmail("katlegondaba08@gmail.com")
                .setPhoneNumber("0625067447")
                .setPassword("Ndaba447")
                .setCreatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createStudent_returnsSavedStudent() {

        when(IStudentRepository.save(any(Student.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Student result = studentService.createStudent(student);

        assertNotNull(result);
        assertEquals("Katlego", result.getFirstName());
        verify(IStudentRepository).save(student);
    }

    @Test
    void getStudentById_returnsStudent() {

        when(IStudentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudentById(1L);

        assertTrue(result.isPresent());
        assertEquals(student.getEmail(), result.get().getEmail());
    }

    @Test
    void getStudentByEmail_returnsStudent() {

        when(IStudentRepository.findByEmail("katlegondaba08@gmail.com"))
                .thenReturn(Optional.of(student));

        Optional<Student> result =
                studentService.getStudentByEmail("katlegondaba08@gmail.com");

        assertTrue(result.isPresent());
    }

    @Test
    void getStudentByPhoneNumber_returnsStudent() {

        when(IStudentRepository.findByPhoneNumber("0625067447"))
                .thenReturn(Optional.of(student));

        Optional<Student> result =
                studentService.getStudentByPhoneNumber("0625067447");

        assertTrue(result.isPresent());
    }

    @Test
    void getAllStudents_returnsList() {

        when(IStudentRepository.findAll())
                .thenReturn(List.of(student));

        List<Student> students = studentService.getAllStudents();

        assertEquals(1, students.size());
    }

    @Test
    void deleteStudent_callsRepository() {

        studentService.deleteStudent(1L);

        verify(IStudentRepository).deleteById(1L);
    }
}
