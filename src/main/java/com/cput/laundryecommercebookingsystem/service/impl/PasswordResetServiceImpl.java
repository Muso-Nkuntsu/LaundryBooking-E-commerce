package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.PasswordResetToken;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IPasswordResetTokenRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IEmailService;
import com.cput.laundryecommercebookingsystem.service.IPasswordResetService;
import com.cput.laundryecommercebookingsystem.util.PasswordHasher;
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

/*PasswordResetServiceImpl.java
 * Secure password reset:
 * - a random 256-bit token; only its SHA-256 hash is stored
 * - the link expires (30 minutes by default) and works once; asking again cancels the old link
 * - the reply is the same whether or not the email has an account, so accounts can't be discovered
 * - the token is never logged
 */
@Service
public class PasswordResetServiceImpl implements IPasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final long REQUEST_COOLDOWN_SECONDS = 60;
    private static final String INVALID_LINK = "This reset link is invalid or has expired. Ask for a new one.";

    private final IStudentRepository studentRepository;
    private final IPasswordResetTokenRepository tokenRepository;
    private final IEmailService emailService;
    private final String frontendUrl;
    private final long expiryMinutes;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetServiceImpl(IStudentRepository studentRepository,
                                    IPasswordResetTokenRepository tokenRepository,
                                    IEmailService emailService,
                                    @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl,
                                    @Value("${app.password-reset.token-expiry-minutes:30}") long expiryMinutes) {
        this.studentRepository = studentRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        this.expiryMinutes = expiryMinutes;
    }

    @Override
    @Transactional
    public void requestReset(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Enter your email address.");
        }
        Optional<Student> found = studentRepository.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty() || found.get().isDisabled()) {
            return; // same reply as when it works
        }
        Student student = found.get();
        LocalDateTime now = LocalDateTime.now();

        Optional<PasswordResetToken> latest = tokenRepository.findFirstByStudentOrderByCreatedAtDesc(student);
        if (latest.isPresent() && latest.get().getCreatedAt().plusSeconds(REQUEST_COOLDOWN_SECONDS).isAfter(now)) {
            return; // ignore repeated clicks quietly
        }

        tokenRepository.deleteByStudent(student); // only the newest link works
        String rawToken = generateToken();
        tokenRepository.save(new PasswordResetToken(student, hash(rawToken), now, now.plusMinutes(expiryMinutes)));

        try {
            emailService.sendPasswordResetEmail(student, frontendUrl + "/reset-password?token=" + rawToken, expiryMinutes);
        } catch (RuntimeException e) {
            log.error("Could not send a password reset email: {}", e.getClass().getSimpleName());
        }
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException(INVALID_LINK);
        }
        PasswordHasher.requireStrong(newPassword);

        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken.trim()))
                .filter(t -> !t.isExpired(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK));

        Student student = token.getStudent();
        student.changePasswordHash(PasswordHasher.hash(newPassword));
        studentRepository.save(student);
        tokenRepository.deleteByStudent(student); // the link can't be used again
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
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
