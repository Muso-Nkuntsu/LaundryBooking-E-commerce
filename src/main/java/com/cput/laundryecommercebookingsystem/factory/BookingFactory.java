/**
 * Muso Nkuntsu
 * 231223722
 * BookingFactory.java
 * 27 July 2026
 */

package com.cput.laundryecommercebookingsystem.factory;


import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;

import java.time.LocalDateTime;

public final class BookingFactory {
    private BookingFactory(){

    }
    public static Booking createBooking(Student student, LaundryMachine laundryMachine,
                                        TimeSlot timeSlot,
                                        double totalAmount){
        return createBooking(student, laundryMachine, timeSlot, null, totalAmount);
    }
    public static Booking createBooking(Student student,
                                        LaundryMachine laundryMachine,
                                        TimeSlot timeSlot,
                                        LaundryService laundryService,
                                        double totalAmount){
        validate(student, laundryMachine,timeSlot,totalAmount);

        Booking.Builder builder = Booking.builder()
                .student(student)
                .laundryMachine(laundryMachine)
                .timeSlot(timeSlot)
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(totalAmount);
        if (laundryService != null) {
            builder.laundryService(laundryService);
        }
        return builder.build();
    }
    public static Booking createBookingWithStatus(Student student,
                                                  LaundryMachine laundryMachine,
                                                  TimeSlot timeSlot,
                                                  LaundryService laundryService,
                                                  double totalAmount,
                                                  BookingStatus status){
        validate(student, laundryMachine,timeSlot,totalAmount);

        if (status == null) {
            throw new IllegalArgumentException("Booking status cannot be null");
        }
        Booking.Builder builder = Booking.builder()
                .student(student)
                .laundryMachine(laundryMachine)
                .timeSlot(timeSlot)
                .bookingDate(LocalDateTime.now())
                .status(status)
                .totalAmount(totalAmount);

        if (laundryService == null) {
            builder.laundryService(laundryService);
        }
        return builder.build();
    }

    public static void validate(Student student,
                                LaundryMachine laundryMachine,
                                TimeSlot timeSlot,
                                double totalAmount) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        if (laundryMachine == null) {
            throw new IllegalArgumentException("Laundry machine cannot be null");
        }
        if (timeSlot == null) {
            throw new IllegalArgumentException("Time slot cannot be null");
        }
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Total amount must be a positive value");
        }
    }
}
