/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */


package com.cput.laundryecommercebookingsystem.service;



import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;

import java.util.List;
import java.util.Optional;

public interface IBookingService {
    Booking createBooking(Student student,
                         LaundryMachine laundryMachine,
                         TimeSlot timeSlot,
                         LaundryService laundryService,
                         double totalAmount);

        Booking cancelBooking(Long bookingId);
        Booking updateStatus(Long bookingId, BookingStatus status);
        Booking deleteBooking(Long bookingId);
        Optional<Booking> findById(Long bookingId);
        List<Booking> findByStudent(Student student);
        List<Booking> findAll();

    }