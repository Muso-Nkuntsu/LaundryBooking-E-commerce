package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*StudentRepository.java
 * Author: Sabotseng Ndaba
 * Date: 28 July 2026
 */

public interface IStudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByPhoneNumber(String phoneNumber);

}