package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.NotificationType;

import java.time.LocalDateTime;

/**
 * Factory responsible for constructing valid {@link Notification} instances.
 * Centralises validation so callers don't build malformed notifications
 * (missing student, blank message, missing type).
 *
 * Internally this still uses Notification.Builder — the factory adds a
 * validation/defaulting layer on top of it, it does not replace it.
 */
public final class NotificationFactory {

    private NotificationFactory() {
        // Static factory — not instantiable
    }

    /**
     * Creates a new, unread notification, timestamped now.
     */
    public static Notification createNotification(Student student,
                                                  String message,
                                                  NotificationType type) {
        validate(student, message, type);

        return Notification.builder()
                .student(student)
                .message(message)
                .type(type)
                .dateSent(LocalDateTime.now())
                .isRead(false)
                .build();
    }

    /**
     * Convenience method for a booking-confirmation notification —
     * builds the standard message text so callers don't duplicate it.
     */
    public static Notification createBookingConfirmation(Student student, Long bookingId) {
        String message = "Your booking (ID: " + bookingId + ") has been confirmed.";
        return createNotification(student, message, NotificationType.BOOKING_CONFIRMATION);
    }

    /**
     * Convenience method for a booking-cancellation notification.
     */
    public static Notification createBookingCancelled(Student student, Long bookingId) {
        String message = "Your booking (ID: " + bookingId + ") has been cancelled.";
        return createNotification(student, message, NotificationType.BOOKING_CANCELLED);
    }

    /**
     * Convenience method for an order-completion notification.
     */
    public static Notification createOrderComplete(Student student, Long orderId) {
        String message = "Your order (ID: " + orderId + ") has been completed.";
        return createNotification(student, message, NotificationType.ORDER_COMPLETE);
    }

    /**
     * Convenience method for a payment-confirmation notification.
     */
    public static Notification createPaymentConfirmation(Student student, Long paymentId) {
        String message = "Your payment (ID: " + paymentId + ") has been confirmed.";
        return createNotification(student, message, NotificationType.PAYMENT_CONFIRMATION);
    }

    private static void validate(Student student, String message, NotificationType type) {
        if (student == null) {
            throw new IllegalArgumentException("student must not be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
    }
}
