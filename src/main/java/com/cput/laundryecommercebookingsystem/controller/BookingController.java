package com.cput.laundryecommercebookingsystem.controller;
/*
* Muso Nkuntsu
* 231223722
* 29 July 2026*/

import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.repository.*;
import com.cput.laundryecommercebookingsystem.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;


@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    /**
     * REST controller exposing CRUD operations for Booking.
     * Works directly against the Booking entity — no DTO layer for now.
     * Business logic (double-booking checks, status transitions) stays in
     * BookingService; this class only resolves incoming IDs to entities
     * and delegates.
     */

        private final IBookingService bookingService;
        private final IStudentRepository studentRepository;
        private final ILaundryMachineRepository laundryMachineRepository;
        private final TimeSlotRepository timeSlotRepository;
        private final ILaundryServiceRepository laundryServiceRepository;

        public BookingController(IBookingService bookingService,
                                 IStudentRepository studentRepository,
                                 ILaundryMachineRepository laundryMachineRepository,
                                 TimeSlotRepository timeSlotRepository,
                                 ILaundryServiceRepository laundryServiceRepository) {
            this.bookingService = bookingService;
            this.studentRepository = studentRepository;
            this.laundryMachineRepository = laundryMachineRepository;
            this.timeSlotRepository = timeSlotRepository;
            this.laundryServiceRepository = laundryServiceRepository;
        }

        // ---------- CREATE ----------

        @PostMapping
        public ResponseEntity<Booking> createBooking(@RequestParam Long studentId,
                                                     @RequestParam Long machineId,
                                                     @RequestParam Long timeSlotId,
                                                     @RequestParam(required = false) Long serviceId,
                                                     @RequestParam double totalAmount) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> notFound("Student", studentId));

            LaundryMachine machine = laundryMachineRepository.findById(machineId)
                    .orElseThrow(() -> notFound("LaundryMachine", machineId));

            TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                    .orElseThrow(() -> notFound("TimeSlot", timeSlotId));

            LaundryService laundryService = null;
            if (serviceId != null) {
                laundryService = laundryServiceRepository.findById(serviceId)
                        .orElseThrow(() -> notFound("LaundryService", serviceId));
            }

            try {
                Booking booking = bookingService.createBooking(
                        student, machine, timeSlot, laundryService, totalAmount);
                return ResponseEntity.status(HttpStatus.CREATED).body(booking);
            } catch (IllegalStateException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }

        // ---------- READ ----------

        @GetMapping
        public ResponseEntity<List<Booking>> getAllBookings() {
            return ResponseEntity.ok(bookingService.findAll());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> notFound("Booking", id));
            return ResponseEntity.ok(booking);
        }

        @GetMapping("/student/{studentId}")
        public ResponseEntity<List<Booking>> getBookingsByStudent(@PathVariable Long studentId) {
            Student student = studentRepository.findById(studentId)
                    .orElseThrow(() -> notFound("Student", studentId));
            return ResponseEntity.ok(bookingService.findByStudent(student));
        }

        // ---------- UPDATE ----------

        @PutMapping("/{id}/status")
        public ResponseEntity<Booking> updateStatus(@PathVariable Long id,
                                                    @RequestParam BookingStatus status) {
            try {
                Booking booking = bookingService.updateStatus(id, status);
                return ResponseEntity.ok(booking);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }

        @PutMapping("/{id}/cancel")
        public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
            try {
                Booking booking = bookingService.cancelBooking(id);
                return ResponseEntity.ok(booking);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }

        // ---------- DELETE ----------

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
            try {
                bookingService.deleteBooking(id);
                return ResponseEntity.noContent().build();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
        }

        // ---------- Helpers ----------

        private ResponseStatusException notFound(String entityName, Long id) {
            return new ResponseStatusException(
                    HttpStatus.NOT_FOUND, entityName + " not found with id: " + id);
        }
    }

