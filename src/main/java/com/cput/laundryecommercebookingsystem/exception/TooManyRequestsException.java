package com.cput.laundryecommercebookingsystem.exception;

/** The student asked for another verification email too soon after the last one (HTTP 429). */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
