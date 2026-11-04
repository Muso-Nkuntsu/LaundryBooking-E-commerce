package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.exception.EmailNotVerifiedException;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IEmailVerificationService;
import com.cput.laundryecommercebookingsystem.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*StudentServiceEmailVerificationTest.java
 * Checks how registration, login and email changes behave with email verification.
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

@ExtendWith(MockitoExtension.class)
class StudentServiceEmailVerificationTest {

    @Mock
    private IStudentRepository studentRepository;
    @Mock
    private IEmailVerificationService emailVerificationService;

    private StudentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new StudentServiceImpl(studentRepository, emailVerificationService);
    }

    private Student.Builder existing(boolean verified) {
        return new Student.Builder()
                .setStudentId(1L)
                .setFirstName("Katlego")
                .setLastName("Ndaba")
                .setEmail("katlego@example.com")
                .setPhoneNumber("0625067447")
                .setPassword(PasswordHasher.hash("secret1"))
                .setCreatedAt(LocalDateTime.now())
                .setEmailVerified(verified)
                .setEmailVerifiedAt(verified ? LocalDateTime.now().minusDays(1) : null);
    }

    @Test
    void createStudent_startsUnverifiedAndSendsTheEmail() {
        when(studentRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        // The request body claims the account is verified; it must be ignored.
        Student request = existing(true).setStudentId(null).setEmail("new@example.com").setPassword("secret1").build();

        Student saved = service.createStudent(request);

        assertFalse(saved.isEmailVerified());
        verify(emailVerificationService).sendVerificationEmail(saved);
    }

    @Test
    void login_isRefusedUntilTheEmailIsVerified() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(existing(false).build()));

        assertThrows(EmailNotVerifiedException.class, () -> service.login("katlego@example.com", "secret1"));
    }

    @Test
    void login_withTheWrongPasswordDoesNotRevealTheVerificationState() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(existing(false).build()));

        assertTrue(service.login("katlego@example.com", "wrong-password").isEmpty());
    }

    @Test
    void login_worksOnceTheEmailIsVerified() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(existing(true).build()));

        assertTrue(service.login("katlego@example.com", "secret1").isPresent());
    }

    @Test
    void updateStudent_keepsVerificationWhenTheEmailIsUnchanged() {
        Student current = existing(true).build();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(current));
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        Student saved = service.updateStudent(existing(false).setPhoneNumber("0821234567").build());

        assertTrue(saved.isEmailVerified());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    void updateStudent_requiresVerificationAgainWhenTheEmailChanges() {
        Student current = existing(true).build();
        when(studentRepository.findById(1L)).thenReturn(Optional.of(current));
        when(studentRepository.findByEmail("changed@example.com")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArgument(0));

        Student saved = service.updateStudent(existing(false).setEmail("changed@example.com").build());

        assertFalse(saved.isEmailVerified());
        assertNull(saved.getEmailVerifiedAt());
        verify(emailVerificationService).sendVerificationEmail(saved);
    }
}
