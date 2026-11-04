package com.cput.laundryecommercebookingsystem.exception;

/** The verification email could not be sent, for example because the mail server is unreachable (HTTP 503). */
public class EmailDeliveryException extends RuntimeException {

    public EmailDeliveryException(String message) {
        super(message);
    }
}
