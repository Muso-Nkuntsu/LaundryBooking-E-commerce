package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Student;

/*IEmailService.java
 * Sends emails to students.
 * Author: Snalo Noni (230541844)
 * Date: 4 November 2026
 */

public interface IEmailService {

    /** Emails the student a link to confirm their address. Throws a RuntimeException if it cannot be sent. */
    void sendVerificationEmail(Student student, String verificationLink);
}
