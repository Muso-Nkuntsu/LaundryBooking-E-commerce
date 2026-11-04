package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Student;

/*IEmailVerificationService.java
 * Creates, sends and checks email verification links.
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

public interface IEmailVerificationService {

    /**
     * Creates a fresh verification link for the student and emails it. Any earlier link stops working.
     * Returns false when the email could not be sent; the student can ask for another one later.
     */
    boolean sendVerificationEmail(Student student);

    /**
     * Confirms the email address that the token was sent to.
     * Throws IllegalArgumentException when the token is unknown, already used or expired.
     */
    Student verifyEmail(String token);

    /**
     * Sends a new verification email. Does nothing when the address is unknown or already verified,
     * so the reply cannot be used to find out which emails have accounts.
     */
    void resendVerificationEmail(String email);
}
