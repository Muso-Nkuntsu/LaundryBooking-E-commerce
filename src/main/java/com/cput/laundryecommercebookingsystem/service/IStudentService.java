package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Student;

import java.util.List;
import java.util.Optional;

/*StudentService.java
 * Service interface for Student.
 * Author: Sabotseng Ndaba (230235875)
 * Date: 28 July 2026
 */

public interface IStudentService {

    Student createStudent(Student student);

    Student updateStudent(Student student);

    void deleteStudent(Long studentId);

    Optional<Student> getStudentById(Long studentId);

    Optional<Student> getStudentByEmail(String email);

    Optional<Student> getStudentByPhoneNumber(String phoneNumber);

    List<Student> getAllStudents();
}
