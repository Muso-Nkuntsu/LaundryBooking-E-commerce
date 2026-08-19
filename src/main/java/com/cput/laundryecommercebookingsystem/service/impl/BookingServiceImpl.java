/*
* Muso Nkuntsu
* 231223722
* 28 July 2026
*/

package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.factory.BookingFactory;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements IBookingService {


    private final iBookingRepository bookingRepository;

    public BookingServiceImpl(iBookingRepository bookingRepository){
        this.bookingRepository = bookingRepository;
    }
    @Override
    @Transactional
    public Booking createBooking(Student student,
                                 LaundryMachine laundryMachine,
                                 TimeSlot timeSlot,
                                 LaundryService laundryService,
                                 double totalAmount){
        bookingRepository.findByLaundryMachineAndTimeSlot(laundryMachine,timeSlot).ifPresent(existing ->{
            throw new IllegalStateException("This machine is already booked for the selected time slot");
        });
        Booking booking = BookingFactory.createBooking(
                student,laundryMachine,timeSlot,laundryService,totalAmount);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        return null;
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
    public Booking deleteBooking(Long bookingId) {
        return null;
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
                .orElseThrow(() -> new IllegalArgumentException("Booking with ID " + bookingId + " not found"));
    }
}
