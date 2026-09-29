package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.enums.NotificationType;
import com.cput.laundryecommercebookingsystem.domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NotificationFactoryTest {

    private Student student;

    @BeforeEach
    void setUp() {
        student = mock(Student.class);
    }

    // ---------- Successful creation ----------

    @Test
    void createNotification_validInput_createsUnreadNotification() {
        Notification notification = NotificationFactory.createNotification(
                student, "Your booking is confirmed.", NotificationType.BOOKING_CONFIRMATION);

        assertNotNull(notification);
        assertEquals(student, notification.getStudent());
        assertEquals("Your booking is confirmed.", notification.getMessage());
        assertEquals(NotificationType.BOOKING_CONFIRMATION, notification.getType());
        assertFalse(notification.isRead());
        assertNotNull(notification.getDateSent());
    }

    @Test
    void createBookingConfirmation_buildsExpectedMessageAndType() {
        Notification notification = NotificationFactory.createBookingConfirmation(student, 101L);

        assertEquals(NotificationType.BOOKING_CONFIRMATION, notification.getType());
        assertTrue(notification.getMessage().contains("101"));
    }

    @Test
    void createBookingCancelled_buildsExpectedMessageAndType() {
        Notification notification = NotificationFactory.createBookingCancelled(student, 102L);

        assertEquals(NotificationType.BOOKING_CANCELLED, notification.getType());
        assertTrue(notification.getMessage().contains("102"));
    }

    @Test
    void createOrderComplete_buildsExpectedMessageAndType() {
        Notification notification = NotificationFactory.createOrderComplete(student, 55L);

        assertEquals(NotificationType.ORDER_COMPLETE, notification.getType());
        assertTrue(notification.getMessage().contains("55"));
    }

    @Test
    void createPaymentConfirmation_buildsExpectedMessageAndType() {
        Notification notification = NotificationFactory.createPaymentConfirmation(student, 9L);

        assertEquals(NotificationType.PAYMENT_CONFIRMATION, notification.getType());
        assertTrue(notification.getMessage().contains("9"));
    }

    // ---------- Validation / invalid input ----------

    @Test
    void createNotification_nullStudent_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createNotification(
                        null, "message", NotificationType.ORDER_COMPLETE));
        assertTrue(ex.getMessage().contains("student"));
    }

    @Test
    void createNotification_nullMessage_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createNotification(
                        student, null, NotificationType.ORDER_COMPLETE));
        assertTrue(ex.getMessage().contains("message"));
    }

    @Test
    void createNotification_blankMessage_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createNotification(
                        student, "   ", NotificationType.ORDER_COMPLETE));
        assertTrue(ex.getMessage().contains("message"));
    }

    @Test
    void createNotification_nullType_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                NotificationFactory.createNotification(student, "message", null));
        assertTrue(ex.getMessage().contains("type"));
    }
}