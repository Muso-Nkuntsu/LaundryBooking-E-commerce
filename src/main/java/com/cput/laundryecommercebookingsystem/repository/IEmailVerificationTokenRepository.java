package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.EmailVerificationToken;
import com.cput.laundryecommercebookingsystem.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*IEmailVerificationTokenRepository.java
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

@Repository
public interface IEmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findFirstByStudentOrderByCreatedAtDesc(Student student);

    void deleteByStudent(Student student);
}
