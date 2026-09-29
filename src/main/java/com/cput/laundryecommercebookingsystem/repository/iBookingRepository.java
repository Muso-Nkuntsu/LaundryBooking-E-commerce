/*
 * Muso Nkuntsu
 * 231223722
 * 28 July 2026
 */


package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface iBookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByStudent(Student student);


    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findByStudentAndStatus(Student student, BookingStatus status);
    Optional<Booking> findByLaundryMachineAndTimeSlot(LaundryMachine laundryMachine, TimeSlot timeSlot);
}
