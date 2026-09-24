/**
 * Notification.java
 * Muso Nkuntsu
 * 231223722
 * Date: 25 July 2026
 */

package com.cput.laundryecommercebookingsystem.domain;

import com.cput.laundryecommercebookingsystem.domain.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * Notification entity — represents a message sent to a student informing
 * them of a system event (booking confirmed/cancelled, order complete,
 * payment confirmed, etc.). Matches UML class 12: NOTIFICATION.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    // FK: studentId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "message", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "date_sent", nullable = false)
    private LocalDateTime dateSent;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    // Required no-arg constructor for JPA/Hibernate
    protected Notification() {
    }

    // Private constructor — instances are only created via the Builder
    private Notification(Builder builder) {
        this.id = builder.id;
        this.student = builder.student;
        this.message = builder.message;
        this.type = builder.type;
        this.dateSent = builder.dateSent;
        this.isRead = builder.isRead;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---------- Behaviour (per UML) ----------

    /**
     * Sends this notification. In a full implementation this would
     * delegate to a notification/email/push service; kept here to
     * satisfy the UML contract for the entity.
     */
    public void sendNotification() {
        this.dateSent = LocalDateTime.now();
    }

    /** Marks this notification as read. */
    public void markAsRead() {
        this.isRead = true;
    }

    // ---------- Getters (no setters) ----------

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public LocalDateTime getDateSent() {
        return dateSent;
    }

    public boolean isRead() {
        return isRead;
    }

    // ---------- equals / hashCode / toString ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification that = (Notification) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", student=" + (student != null ? student.getStudentId() : null) +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", dateSent=" + dateSent +
                ", isRead=" + isRead +
                '}';
    }

    // ---------- Builder ----------

    public static class Builder {
        private Long id;
        private Student student;
        private String message;
        private NotificationType type;
        private LocalDateTime dateSent;
        private boolean isRead;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder student(Student student) {
            this.student = student;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder dateSent(LocalDateTime dateSent) {
            this.dateSent = dateSent;
            return this;
        }

        public Builder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }

        public Notification build() {
            Objects.requireNonNull(student, "student is required");
            Objects.requireNonNull(message, "message is required");
            Objects.requireNonNull(type, "type is required");
            if (dateSent == null) {
                dateSent = LocalDateTime.now();
            }
            return new Notification(this);
        }
    }
}

