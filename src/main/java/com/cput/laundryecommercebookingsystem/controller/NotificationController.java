package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.INotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Notifications for a student (booking confirmed, booking cancelled, and so on).
 * They are created automatically by BookingServiceImpl; this controller only reads them
 * and marks them as read.
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final INotificationService notificationService;
    private final IStudentRepository studentRepository;

    public NotificationController(INotificationService notificationService,
                                  IStudentRepository studentRepository) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
    }

    /** All notifications for a student, newest first. */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Notification>> getByStudent(@PathVariable Long studentId) {
        List<Notification> notifications = notificationService.findByStudent(getStudentOrThrow(studentId))
                .stream()
                .sorted(Comparator.comparing(Notification::getDateSent).reversed())
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/student/{studentId}/unread")
    public ResponseEntity<List<Notification>> getUnreadByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.findUnreadByStudent(getStudentOrThrow(studentId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/student/{studentId}/read-all")
    public ResponseEntity<List<Notification>> markAllAsRead(@PathVariable Long studentId) {
        return ResponseEntity.ok(notificationService.markAllAsRead(getStudentOrThrow(studentId)));
    }

    private Student getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));
    }
}
