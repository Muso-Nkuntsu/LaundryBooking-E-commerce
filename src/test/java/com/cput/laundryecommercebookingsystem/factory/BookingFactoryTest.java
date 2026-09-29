package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BookingFactoryTest {

    private Student student;
    private LaundryMachine laundryMachine;
    private TimeSlot timeSlot;
    private LaundryService laundryService;

    @BeforeEach
    void setUp() {
        // Mocked collaborators — Booking only needs references to these,
        // not their internal behaviour, so mocks are sufficient here.
        student = mock(Student.class);
        laundryMachine = mock(LaundryMachine.class);
        timeSlot = mock(TimeSlot.class);
        laundryService = mock(LaundryService.class);
    }

    // ---------- Successful creation ----------

    @Test
    void createBooking_withoutService_createsConfirmedBooking() {
        Booking booking = BookingFactory.createBooking(student, laundryMachine, timeSlot, 50.0);

        assertNotNull(booking);
        assertEquals(student, booking.getStudent());
        assertEquals(laundryMachine, booking.getLaundryMachine());
        assertEquals(timeSlot, booking.getTimeSlot());
        assertNull(booking.getLaundryService());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(50.0, booking.getTotalAmount());
        assertNotNull(booking.getBookingDate());
    }

    @Test
    void createBooking_withService_attachesService() {
        Booking booking = BookingFactory.createBooking(
                student, laundryMachine, timeSlot, laundryService, 75.0);

        assertEquals(laundryService, booking.getLaundryService());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void createBookingWithStatus_setsProvidedStatus() {
        Booking booking = BookingFactory.createBookingWithStatus(
                student, laundryMachine, timeSlot, null, 30.0, BookingStatus.COMPLETED);

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
    }

    // ---------- Validation / invalid input ----------

    @Test
    void createBooking_nullStudent_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingFactory.createBooking(null, laundryMachine, timeSlot, 50.0));
        assertTrue(ex.getMessage().contains("student"));
    }

    @Test
    void createBooking_nullLaundryMachine_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingFactory.createBooking(student, null, timeSlot, 50.0));
        assertTrue(ex.getMessage().contains("laundryMachine"));
    }

    @Test
    void createBooking_nullTimeSlot_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingFactory.createBooking(student, laundryMachine, null, 50.0));
        assertTrue(ex.getMessage().contains("timeSlot"));
    }

    @Test
    void createBooking_negativeTotalAmount_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingFactory.createBooking(student, laundryMachine, timeSlot, -10.0));
        assertTrue(ex.getMessage().contains("totalAmount"));
    }

    @Test
    void createBookingWithStatus_nullStatus_throwsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                BookingFactory.createBookingWithStatus(
                        student, laundryMachine, timeSlot, null, 50.0, null));
        assertTrue(ex.getMessage().contains("status"));
    }
}