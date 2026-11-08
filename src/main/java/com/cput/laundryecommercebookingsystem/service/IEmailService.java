package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Student;

public interface IEmailService {
    void sendVerificationEmail(Student student, String verificationLink);
    void sendPasswordResetEmail(Student student, String resetLink, long expiryMinutes);
}
