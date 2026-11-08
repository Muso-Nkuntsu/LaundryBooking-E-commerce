package com.cput.laundryecommercebookingsystem.exception;

/** The password was right, but the student has not confirmed their email address yet (HTTP 403). */
public class EmailNotVerifiedException extends RuntimeException {

    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
