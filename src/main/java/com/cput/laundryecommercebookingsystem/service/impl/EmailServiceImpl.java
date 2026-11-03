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
 * When no mail server is set up, the link is printed in the backend console instead,
 * so registration can still be tested on a developer machine.
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String from;
    private final long expiryHours;

    public EmailServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                            @Value("${app.mail.from:${spring.mail.username:no-reply@laundrybooking.local}}") String from,
                            @Value("${app.verification.token-expiry-hours:24}") long expiryHours) {
        this.mailSender = mailSender;
        this.from = from;
        this.expiryHours = expiryHours;
    }

    @Override
    public void sendVerificationEmail(Student student, String verificationLink) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.warn("No mail server is configured (spring.mail.host). Verification link for {}: {}",
                    student.getEmail(), verificationLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(student.getEmail());
        message.setSubject("Verify your email address");
        message.setText("Hi " + student.getFirstName() + ",\n\n"
                + "Thanks for registering. Please confirm your email address by opening this link:\n\n"
                + verificationLink + "\n\n"
                + "The link works for " + expiryHours + " hours. "
                + "If you did not create this account, you can ignore this email.\n");
        sender.send(message);
    }
}
