/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */


package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.factory.NotificationFactory;
import com.cput.laundryecommercebookingsystem.repository.iNotificationRepository;
import com.cput.laundryecommercebookingsystem.service.INotificationService;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class NotificationServiceImpl implements INotificationService {

    private final iNotificationRepository notificationRepository;

    public NotificationServiceImpl(iNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional
    public Notification sendBookingConfirmation(Student student, Long bookingId) {
        Notification notification = NotificationFactory.createBookingConfirmation(student, bookingId);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification sendBookingCancelled(Student student, Long bookingId) {
        Notification notification = NotificationFactory.createBookingCancelled(student, bookingId);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification sendOrderComplete(Student student, Long orderId) {
        Notification notification = NotificationFactory.createOrderComplete(student, orderId);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification sendPaymentConfirmation(Student student, Long paymentId) {
        Notification notification = NotificationFactory.createPaymentConfirmation(student, paymentId);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No notification found with id: " + notificationId));
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    @Override
    public List<Notification> findByStudent(Student student) {
        return notificationRepository.findByStudent(student);
    }

    @Override
    public List<Notification> findUnreadByStudent(Student student) {
        return notificationRepository.findByStudentAndIsReadFalse(student);
    }


}
