package com.cput.laundryecommercebookingsystem.config;

import com.cput.laundryecommercebookingsystem.exception.EmailDeliveryException;
import com.cput.laundryecommercebookingsystem.exception.EmailNotVerifiedException;
import com.cput.laundryecommercebookingsystem.exception.TooManyRequestsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Turns exceptions thrown by any controller or service into a clear HTTP status
 * with a plain-text message the frontend can show to the student.
 * Unexpected errors never send internal details (stack traces, SQL) to the browser.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    /** Logged in, but the record or action belongs to someone else (or needs an admin). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<String> handleForbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to do that.");
    }

    /** No valid login token was sent. */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<String> handleNotLoggedIn(AuthenticationCredentialsNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in.");
    }

    /** Missing or wrongly typed values, or JSON that can't be read. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<String> handleMalformed(Exception ex) {
        return ResponseEntity.badRequest().body("The request is missing information or has an invalid value.");
    }

    /** A picture bigger than spring.servlet.multipart.max-file-size. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("The picture must be 2 MB or smaller.");
    }

    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<String> handleBadUpload(Exception ex) {
        return ResponseEntity.badRequest().body("Choose a picture to upload.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoRoute(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<String> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body("Method not allowed.");
    }

    /** Anything unexpected: the details go to the server log only, with a reference to match them up. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleUnexpected(Exception ex) {
        String ref = UUID.randomUUID().toString().substring(0, 8);
        log.error("Unhandled error [{}]", ref, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Something went wrong. Please try again later. (ref " + ref + ")");
    }
}
