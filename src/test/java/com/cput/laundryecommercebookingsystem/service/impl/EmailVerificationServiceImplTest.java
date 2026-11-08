package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.EmailVerificationToken;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.exception.EmailDeliveryException;
import com.cput.laundryecommercebookingsystem.exception.TooManyRequestsException;
import com.cput.laundryecommercebookingsystem.repository.IEmailVerificationTokenRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*EmailVerificationServiceImplTest.java
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Mock
    private IEmailVerificationTokenRepository tokenRepository;
    @Mock
    private IStudentRepository studentRepository;
    @Mock
    private IEmailService emailService;

    private EmailVerificationServiceImpl service;
    private Student student;

    @BeforeEach
    void setUp() {
        service = new EmailVerificationServiceImpl(
                tokenRepository, studentRepository, emailService, "http://localhost:5173/", 24, 60);

        student = new Student.Builder()
                .setStudentId(1L)
                .setFirstName("Katlego")
                .setLastName("Ndaba")
                .setEmail("katlego@example.com")
                .setPhoneNumber("0625067447")
                .setPassword("hashed")
                .setCreatedAt(LocalDateTime.now())
                .build();
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void sendVerificationEmail_storesOnlyTheHashAndEmailsTheLink() throws Exception {
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(i -> i.getArgument(0));

        assertTrue(service.sendVerificationEmail(student));

        ArgumentCaptor<EmailVerificationToken> saved = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).deleteByStudent(student);
        verify(tokenRepository).save(saved.capture());

        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(student), link.capture());

        String prefix = "http://localhost:5173/verify-email?token=";
        assertTrue(link.getValue().startsWith(prefix));
        String rawToken = link.getValue().substring(prefix.length());

        assertNotEquals(rawToken, saved.getValue().getTokenHash());
        assertEquals(sha256(rawToken), saved.getValue().getTokenHash());
        assertTrue(saved.getValue().getExpiresAt().isAfter(LocalDateTime.now().plusHours(23)));
    }

    @Test
    void sendVerificationEmail_returnsFalseWhenTheMailServerFails() {
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("smtp down")).when(emailService).sendVerificationEmail(any(), anyString());

        assertFalse(service.sendVerificationEmail(student));
    }

    @Test
    void verifyEmail_marksTheStudentVerified() throws Exception {
        EmailVerificationToken token = new EmailVerificationToken(
                student, sha256("good-token"), LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenHash(sha256("good-token"))).thenReturn(Optional.of(token));
        when(studentRepository.save(student)).thenReturn(student);

        Student result = service.verifyEmail("good-token");

        assertTrue(result.isEmailVerified());
        assertNotNull(result.getEmailVerifiedAt());
        assertTrue(token.isUsed());
    }

    @Test
    void verifyEmail_rejectsAnExpiredToken() throws Exception {
        EmailVerificationToken token = new EmailVerificationToken(
                student, sha256("old-token"), LocalDateTime.now().minusHours(30), LocalDateTime.now().minusHours(6));
        when(tokenRepository.findByTokenHash(sha256("old-token"))).thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.verifyEmail("old-token"));

        assertTrue(ex.getMessage().contains("expired"));
        assertFalse(student.isEmailVerified());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void verifyEmail_rejectsAnUnknownToken() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.verifyEmail("made-up"));
        verify(studentRepository, never()).save(any());
    }

    @Test
    void verifyEmail_rejectsABlankToken() {
        assertThrows(IllegalArgumentException.class, () -> service.verifyEmail("  "));
        assertThrows(IllegalArgumentException.class, () -> service.verifyEmail(null));
    }

    @Test
    void verifyEmail_rejectsATokenThatWasAlreadyUsed() throws Exception {
        EmailVerificationToken token = new EmailVerificationToken(
                student, sha256("used-token"), LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
        token.markUsed(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByTokenHash(sha256("used-token"))).thenReturn(Optional.of(token));

        assertThrows(IllegalArgumentException.class, () -> service.verifyEmail("used-token"));
    }

    @Test
    void verifyEmail_isHarmlessWhenTheAccountIsAlreadyVerified() throws Exception {
        student.markEmailVerified(LocalDateTime.now().minusDays(1));
        EmailVerificationToken token = new EmailVerificationToken(
                student, sha256("repeat"), LocalDateTime.now().minusMinutes(5), LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByTokenHash(sha256("repeat"))).thenReturn(Optional.of(token));

        assertTrue(service.verifyEmail("repeat").isEmailVerified());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void resendVerificationEmail_doesNothingForAnUnknownEmail() {
        when(studentRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        service.resendVerificationEmail("Nobody@Example.com ");

        verifyNoInteractions(emailService);
    }

    @Test
    void resendVerificationEmail_doesNothingWhenAlreadyVerified() {
        student.markEmailVerified(LocalDateTime.now());
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(student));

        service.resendVerificationEmail("katlego@example.com");

        verifyNoInteractions(emailService);
    }

    @Test
    void resendVerificationEmail_isRefusedDuringTheCooldown() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(student));
        when(tokenRepository.findFirstByStudentOrderByCreatedAtDesc(student)).thenReturn(Optional.of(
                new EmailVerificationToken(student, "hash", LocalDateTime.now().minusSeconds(10),
                        LocalDateTime.now().plusHours(24))));

        assertThrows(TooManyRequestsException.class, () -> service.resendVerificationEmail("katlego@example.com"));
        verifyNoInteractions(emailService);
    }

    @Test
    void resendVerificationEmail_sendsANewLinkAfterTheCooldown() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(student));
        when(tokenRepository.findFirstByStudentOrderByCreatedAtDesc(student)).thenReturn(Optional.of(
                new EmailVerificationToken(student, "hash", LocalDateTime.now().minusMinutes(5),
                        LocalDateTime.now().plusHours(24))));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(i -> i.getArgument(0));

        service.resendVerificationEmail("katlego@example.com");

        verify(emailService).sendVerificationEmail(eq(student), anyString());
    }

    @Test
    void resendVerificationEmail_reportsAMailFailure() {
        when(studentRepository.findByEmail("katlego@example.com")).thenReturn(Optional.of(student));
        when(tokenRepository.findFirstByStudentOrderByCreatedAtDesc(student)).thenReturn(Optional.empty());
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("smtp down")).when(emailService).sendVerificationEmail(any(), anyString());

        assertThrows(EmailDeliveryException.class, () -> service.resendVerificationEmail("katlego@example.com"));
    }
}
