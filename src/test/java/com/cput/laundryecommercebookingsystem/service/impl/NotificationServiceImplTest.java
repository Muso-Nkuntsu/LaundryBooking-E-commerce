/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */

package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.enums.NotificationType;
import com.cput.laundryecommercebookingsystem.repository.iNotificationRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationServiceImplTest {
    @Mock
    private iNotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = mock(Student.class);
    }

    @Test
    void sendBookingConfirmation_savesNotificationWithCorrectType() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.sendBookingConfirmation(student, 100L);

        assertEquals(NotificationType.BOOKING_CONFIRMATION, result.getType());
        assertTrue(result.getMessage().contains("100"));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void sendPaymentConfirmation_savesNotificationWithCorrectType() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.sendPaymentConfirmation(student, 7L);

        assertEquals(NotificationType.PAYMENT_CONFIRMATION, result.getType());
    }

    @Test
    void markAsRead_existingNotification_setsReadTrue() {
        Notification notification = Notification.builder()
                .student(student)
                .message("Test")
                .type(NotificationType.ORDER_COMPLETE)
                .dateSent(LocalDateTime.now())
                .isRead(false)
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.markAsRead(1L);

        assertTrue(result.isRead());
    }

    @Test
    void markAsRead_nonExistentId_throwsException() {
        when(notificationRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> notificationService.markAsRead(42L));
    }

    @Test
    void findUnreadByStudent_delegatesToRepository() {
        List<Notification> expected = List.of(mock(Notification.class));
        when(notificationRepository.findByStudentAndIsReadFalse(student)).thenReturn(expected);

        List<Notification> result = notificationService.findUnreadByStudent(student);

        assertEquals(expected, result);
    }
}

