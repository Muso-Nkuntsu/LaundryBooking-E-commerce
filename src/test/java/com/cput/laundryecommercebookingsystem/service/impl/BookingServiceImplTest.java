/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */

package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class BookingServiceImplTest {
    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private iBookingRepository bookingRepository;


    private Student student;

    private LaundryMachine laundryMachine;
    private TimeSlot timeSlot;

    @BeforeEach
    void setUp(){
        student = mock(Student.class);
        laundryMachine = mock(LaundryMachine.class);
        timeSlot = mock(TimeSlot.class);
    }

    @Test
    void createBooking_noExistingConflict_savesBooking(){
        when(bookingRepository.findByLaundryMachineAndTimeSlot(laundryMachine, timeSlot))
                .thenReturn(Optional.empty());

        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Booking result = bookingService.createBooking(student,laundryMachine,timeSlot,null,50.5);

        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }
    @Test
    void createBooking_conflictingSlot_throwsException() {
        Booking existing = mock(Booking.class);
        when(bookingRepository.findByLaundryMachineAndTimeSlot(laundryMachine, timeSlot))
                .thenReturn(Optional.of(existing));

        assertThrows(IllegalStateException.class, () ->
                bookingService.createBooking(student, laundryMachine, timeSlot, null, 50.0));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_existingBooking_updatesStatusToCancelled() {
        Booking booking = Booking.builder()
                .student(student)
                .laundryMachine(laundryMachine)
                .timeSlot(timeSlot)
                .bookingDate(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(50.0)
                .build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelBooking_nonExistentId_throwsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> bookingService.cancelBooking(99L));
    }

    @Test
    void updateStatus_nullStatus_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                bookingService.updateStatus(1L, null));
        verifyNoInteractions(bookingRepository);
    }
    @Test
    void findByStudent_delegatesToRepository() {
        List<Booking> expected = List.of(mock(Booking.class));
        when(bookingRepository.findByStudent(student)).thenReturn(expected);

        List<Booking> result = bookingService.findByStudent(student);

        assertEquals(expected, result);
    }
}


