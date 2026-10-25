/*
* Muso Nkuntsu
* 231223722
* 28 July 2026
*/

package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.factory.BookingFactory;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import com.cput.laundryecommercebookingsystem.service.INotificationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BookingServiceImpl implements IBookingService {

    private final iBookingRepository bookingRepository;
    private final INotificationService notificationService;

    public BookingServiceImpl(iBookingRepository bookingRepository,
                              INotificationService notificationService){
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Booking createBooking(Student student,
                                 LaundryMachine laundryMachine,
                                 TimeSlot timeSlot,
                                 LaundryService laundryService,
                                 double totalAmount){
        BookingFactory.validate(student, laundryMachine, timeSlot, totalAmount);

        if (laundryMachine.getStatus() == MachineStatus.OUT_OF_ORDER) {
            throw new IllegalStateException("This machine is out of order and cannot be booked");
        }

        // A cancelled booking frees the slot, so only bookings that are still active block it.
        if (bookingRepository.existsByLaundryMachineAndTimeSlotAndStatusNot(
                laundryMachine, timeSlot, BookingStatus.CANCELLED)) {
            throw new IllegalStateException("This machine is already booked for the selected time slot");
        }

        // A booking that costs money stays PENDING until it is paid for (see PaymentServiceImpl).
        // A free booking (no service added) has nothing to pay, so it is confirmed straight away.
        // A PENDING booking still holds the machine, so nobody else can take it while the student pays.
        BookingStatus status = totalAmount > 0 ? BookingStatus.PENDING : BookingStatus.CONFIRMED;
        Booking booking = BookingFactory.createBookingWithStatus(
                student, laundryMachine, timeSlot, laundryService, totalAmount, status);
        Booking saved = bookingRepository.save(booking);

        if (status == BookingStatus.CONFIRMED) {
            notificationService.sendBookingConfirmation(student, saved.getId());
        }
        return saved;
    }

    /** Called once a booking has been paid for: PENDING becomes CONFIRMED. */
    @Override
    @Transactional
    public Booking confirmBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return booking;
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only a booking that is waiting for payment can be confirmed");
        }

        booking.createBooking(); // sets CONFIRMED
        Booking saved = bookingRepository.save(booking);

        notificationService.sendBookingConfirmation(saved.getStudent(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("A completed booking cannot be cancelled");
        }

        booking.cancelBooking();
        Booking saved = bookingRepository.save(booking);

        notificationService.sendBookingCancelled(saved.getStudent(), saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Booking updateStatus(Long bookingId, BookingStatus status){
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        Booking booking = getBookingOrThrow(bookingId);
        booking.updateStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking deleteBooking(Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        bookingRepository.delete(booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(Long bookingId){
        return bookingRepository.findById(bookingId);
    }
    @Override
    public List<Booking> findAll(){
        return bookingRepository.findAll();
    }
    @Override
    public List<Booking> findByStudent(Student student) {
        return bookingRepository.findByStudent(student);
    }
    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking with ID " + bookingId + " not found"));
    }
}
