/**
 * Booking.java
 * Muso Nkuntsu
 * 231223722
 * Date: 25 July 2026
 */

package com.cput.laundryecommercebookingsystem.domain;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Booking entity — represents a student's reservation of a laundry machine
 * for a specific time slot, optionally attached to a laundry service.
 *
 * NOTE: LaundryRoom is intentionally NOT referenced directly here.
 * A booking is tied to a specific LaundryMachine, and every machine
 * already belongs to exactly one LaundryRoom. Storing roomId separately
 * on Booking would risk inconsistent data (e.g. a booking's room field
 * disagreeing with the room its machine actually belongs to). The room
 * is always resolved via booking.getLaundryMachine().getLaundryRoom().
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long id;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    // FK: studentId — student who made the booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // FK: machineId — the specific machine reserved (room is derived from this)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private LaundryMachine laundryMachine;

    // FK: timeSlotId — the reserved time slot
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    // FK: serviceId — optional laundry service attached to this booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private LaundryService laundryService;

    // Required no-arg constructor for JPA/Hibernate
    protected Booking() {
    }

    // Private constructor — instances are only created via the Builder
    private Booking(Builder builder) {
        this.id = builder.id;
        this.bookingDate = builder.bookingDate;
        this.status = builder.status;
        this.totalAmount = builder.totalAmount;
        this.student = builder.student;
        this.laundryMachine = builder.laundryMachine;
        this.timeSlot = builder.timeSlot;
        this.laundryService = builder.laundryService;
    }

    public static Builder builder() {
        return new Builder();
    }

    // ---------- Behaviour (per UML) ----------

    /** Marks this booking as created/confirmed. */
    public void createBooking() {
        this.status = BookingStatus.CONFIRMED;
    }

    /** Cancels this booking. */
    public void cancelBooking() {
        this.status = BookingStatus.CANCELLED;
    }

    /** Updates the booking's status to the given value. */
    public void updateStatus(BookingStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * Convenience accessor — resolves the LaundryRoom via this booking's
     * machine, rather than storing a separate (and potentially
     * inconsistent) room reference on Booking itself.
     */
    public LaundryRoom getLaundryRoom() {
        return laundryMachine != null ? laundryMachine.getLaundryRoom() : null;
    }

    // ---------- Getters (no setters — immutable outside of behaviour methods) ----------

    public Long getId() {
        return id;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public Student getStudent() {
        return student;
    }

    public LaundryMachine getLaundryMachine() {
        return laundryMachine;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public LaundryService getLaundryService() {
        return laundryService;
    }

    // ---------- equals / hashCode / toString ----------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingDate=" + bookingDate +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", student=" + (student != null ? student.getStudentId() : null) +
                ", laundryMachine=" + (laundryMachine != null ? laundryMachine.getMachineId() : null) +
                ", timeSlot=" + (timeSlot != null ? timeSlot.getId() : null) +
                ", laundryService=" + (laundryService != null ? laundryService.getId() : null) +
                '}';
    }

    // ---------- Builder ----------

    public static class Builder {
        private Long id;
        private LocalDateTime bookingDate;
        private BookingStatus status;
        private double totalAmount;
        private Student student;
        private LaundryMachine laundryMachine;
        private TimeSlot timeSlot;
        private LaundryService laundryService;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder bookingDate(LocalDateTime bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        public Builder status(BookingStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalAmount(double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder student(Student student) {
            this.student = student;
            return this;
        }

        public Builder laundryMachine(LaundryMachine laundryMachine) {
            this.laundryMachine = laundryMachine;
            return this;
        }

        public Builder timeSlot(TimeSlot timeSlot) {
            this.timeSlot = timeSlot;
            return this;
        }

        public Builder laundryService(LaundryService laundryService) {
            this.laundryService = laundryService;
            return this;
        }

        public Booking build() {
            Objects.requireNonNull(student, "student is required");
            Objects.requireNonNull(laundryMachine, "laundryMachine is required");
            Objects.requireNonNull(timeSlot, "timeSlot is required");
            Objects.requireNonNull(bookingDate, "bookingDate is required");
            Objects.requireNonNull(status, "status is required");
            return new Booking(this);
        }
    }
}
