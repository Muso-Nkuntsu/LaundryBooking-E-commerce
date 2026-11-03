package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.EmailVerificationToken;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.exception.EmailDeliveryException;
import com.cput.laundryecommercebookingsystem.exception.TooManyRequestsException;
import com.cput.laundryecommercebookingsystem.repository.IEmailVerificationTokenRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IEmailService;
import com.cput.laundryecommercebookingsystem.service.IEmailVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/*EmailVerificationServiceImpl.java
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

@Service
public class EmailVerificationServiceImpl implements IEmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);
    private static final int TOKEN_BYTES = 32;

    private final IEmailVerificationTokenRepository tokenRepository;
    private final IStudentRepository studentRepository;
    private final IEmailService emailService;
    private final String frontendUrl;
    private final long expiryHours;
    private final long resendCooldownSeconds;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationServiceImpl(IEmailVerificationTokenRepository tokenRepository,
                                        IStudentRepository studentRepository,
                                        IEmailService emailService,
                                        @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
                                        @Value("${app.verification.token-expiry-hours:24}") long expiryHours,
                                        @Value("${app.verification.resend-cooldown-seconds:60}") long resendCooldownSeconds) {
        this.tokenRepository = tokenRepository;
        this.studentRepository = studentRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        this.expiryHours = expiryHours;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    @Override
    @Transactional
    public boolean sendVerificationEmail(Student student) {
        // Only the newest link works.
        tokenRepository.deleteByStudent(student);

        String rawToken = generateToken();
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.save(new EmailVerificationToken(student, hash(rawToken), now, now.plusHours(expiryHours)));

        try {
            emailService.sendVerificationEmail(student, frontendUrl + "/verify-email?token=" + rawToken);
            return true;
        } catch (RuntimeException e) {
            // Not rethrown: that would roll back the registration. The student can ask for a new email.
            log.error("Could not send the verification email to {}: {}", student.getEmail(), e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public Student verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("The verification link is invalid.");
        }

        EmailVerificationToken stored = tokenRepository.findByTokenHash(hash(token.trim()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "This verification link is invalid. Request a new one."));

        Student student = stored.getStudent();
        LocalDateTime now = LocalDateTime.now();

        // Opening the same link twice (or a mail app pre-opening it) should not look like a failure.
        if (student.isEmailVerified()) {
            return student;
        }
        if (stored.isUsed()) {
            throw new IllegalArgumentException("This verification link has already been used.");
        }
        if (stored.isExpired(now)) {
            throw new IllegalArgumentException("This verification link has expired. Request a new one.");
        }

        stored.markUsed(now);
        tokenRepository.save(stored);

        student.markEmailVerified(now);
        return studentRepository.save(student);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Enter your email address.");
        }

        Optional<Student> found = studentRepository.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty() || found.get().isEmailVerified()) {
            return;
        }
        Student student = found.get();

        Optional<EmailVerificationToken> latest = tokenRepository.findFirstByStudentOrderByCreatedAtDesc(student);
        if (latest.isPresent()
                && latest.get().getCreatedAt().plusSeconds(resendCooldownSeconds).isAfter(LocalDateTime.now())) {
            throw new TooManyRequestsException("Please wait a minute before asking for another verification email.");
        }

        if (!sendVerificationEmail(student)) {
            // Rolls back, so a link that was never delivered is not left behind.
            throw new EmailDeliveryException("We couldn't send the email right now. Please try again later.");
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
