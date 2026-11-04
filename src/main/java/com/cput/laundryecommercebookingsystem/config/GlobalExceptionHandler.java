package com.cput.laundryecommercebookingsystem.config;

import com.cput.laundryecommercebookingsystem.exception.EmailDeliveryException;
import com.cput.laundryecommercebookingsystem.exception.EmailNotVerifiedException;
import com.cput.laundryecommercebookingsystem.exception.TooManyRequestsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * Turns exceptions thrown by any controller or service into a clear HTTP status
 * with a plain-text message the frontend can show to the student.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Something that was asked for does not exist. */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /** The request itself is wrong (missing or invalid values). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /** The request is valid but clashes with the current data (for example a double booking). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /** The student has not confirmed their email address yet. */
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<String> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /** Another verification email was asked for too soon. */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<String> handleTooManyRequests(TooManyRequestsException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    /** The email could not be sent. */
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<String> handleEmailDelivery(EmailDeliveryException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }

    /** The database refused the change (for example a duplicate value in a unique column). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("This conflicts with existing data. Check for duplicates or records that are still in use.");
    }
}
