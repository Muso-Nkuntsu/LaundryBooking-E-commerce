package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Notification;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.security.CurrentStudent;
import com.cput.laundryecommercebookingsystem.service.INotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Notifications for a student (booking confirmed, booking cancelled, and so on).
 * They are created automatically by BookingServiceImpl; this controller only reads them
 * and marks them as read. Security: a student only sees and changes their own notifications.
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final INotificationService notificationService;
    private final IStudentRepository studentRepository;
    private final CurrentStudent currentStudent;

    public NotificationController(INotificationService notificationService,
                                  IStudentRepository studentRepository,
                                  CurrentStudent currentStudent) {
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.currentStudent = currentStudent;
    }

    /** All notifications for a student, newest first. */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Notification>> getByStudent(@PathVariable Long studentId) {
        currentStudent.checkOwner(studentId);
        List<Notification> notifications = notificationService.findByStudent(getStudentOrThrow(studentId))
                .stream()
                .sorted(Comparator.comparing(Notification::getDateSent).reversed())
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/student/{studentId}/unread")
    public ResponseEntity<List<Notification>> getUnreadByStudent(@PathVariable Long studentId) {
        currentStudent.checkOwner(studentId);
        return ResponseEntity.ok(notificationService.findUnreadByStudent(getStudentOrThrow(studentId)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found with id: " + id));
        currentStudent.checkOwner(notification.getStudent());
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/student/{studentId}/read-all")
    public ResponseEntity<List<Notification>> markAllAsRead(@PathVariable Long studentId) {
        currentStudent.checkOwner(studentId);
        return ResponseEntity.ok(notificationService.markAllAsRead(getStudentOrThrow(studentId)));
    }

    private Student getStudentOrThrow(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NoSuchElementException("Student not found with id: " + studentId));
    }
}
