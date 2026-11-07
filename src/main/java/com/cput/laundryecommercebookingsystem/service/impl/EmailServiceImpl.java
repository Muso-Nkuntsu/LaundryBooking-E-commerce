package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/*EmailServiceImpl.java
 * Sends mail through the SMTP server set in application.properties (spring.mail.*).
 * Links contain secret tokens, so they are only printed in the console when no mail server
 * is set AND app.mail.log-links=true (on your own machine only, never on a shared server).
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final long expiryHours;
    private final boolean logLinks;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                            @Value("${app.mail.from:${spring.mail.username:no-reply@laundrybooking.local}}") String from,
                            @Value("${app.verification.token-expiry-hours:24}") long expiryHours,
                            @Value("${app.mail.log-links:false}") boolean logLinks) {
        this.mailSender = mailSender;
        this.from = from;
        this.expiryHours = expiryHours;
        this.logLinks = logLinks;
    }

    @Override
    public void sendVerificationEmail(Student student, String verificationLink) {
        send(student, "Verify your email address", "Hi " + student.getFirstName() + ",\n\n"
                + "Thanks for registering. Please confirm your email address by opening this link:\n\n"
                + verificationLink + "\n\n"
                + "The link works for " + expiryHours + " hours. "
                + "If you did not create this account, you can ignore this email.\n", verificationLink);
    }

    @Override
    public void sendPasswordResetEmail(Student student, String resetLink, long expiryMinutes) {
        send(student, "Reset your password", "Hi " + student.getFirstName() + ",\n\n"
                + "We received a request to reset your password. Open this link to choose a new one:\n\n"
                + resetLink + "\n\n"
                + "The link works for " + expiryMinutes + " minutes and can be used once. "
                + "If you didn't ask for this, you can ignore this email.\n", resetLink);
    }

    private void send(Student student, String subject, String body, String link) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            if (logLinks) {
                log.warn("[DEV ONLY] No mail server is set up. '{}' link for student {}: {}",
                        subject, student.getStudentId(), link);
            } else {
                log.warn("No mail server is set up (spring.mail.host), so the '{}' email was not sent. "
                        + "For local testing, set MAIL_LOG_LINKS=true to print the link here.", subject);
            }
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(student.getEmail());
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }
}
