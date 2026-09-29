/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */

package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;

import java.util.*;

public interface INotificationService {
    Notification sendBookingConfirmation(Student student, Long bookingId);

    Notification sendBookingCancelled(Student student, Long bookingId);

    Notification sendOrderComplete(Student student, Long orderId);

    Notification sendPaymentConfirmation(Student student, Long paymentId);

    Notification markAsRead(Long notificationId);

    Optional<Notification> findById(Long notificationId);

    List<Notification> findByStudent(Student student);

    List<Notification> findUnreadByStudent(Student student);
}
