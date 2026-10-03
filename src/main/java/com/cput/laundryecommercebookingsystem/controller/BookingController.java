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
import java.util.List;
import java.util.NoSuchElementException;


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

        /**
         * Example: POST /api/bookings?studentId=1&machineId=2&timeSlotId=3&serviceId=4
         * The total is worked out here from the chosen service, never taken from the request,
         * so it cannot be changed in the browser.
         */
        @PostMapping
        public ResponseEntity<Booking> createBooking(@RequestParam Long studentId,
                                                     @RequestParam Long machineId,
                                                     @RequestParam Long timeSlotId,
                                                     @RequestParam(required = false) Long serviceId) {
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

            double totalAmount = laundryService != null ? laundryService.getPrice() : 0.0;

            // A double booking gives 409 and invalid values give 400 (see GlobalExceptionHandler).
            Booking booking = bookingService.createBooking(
                    student, machine, timeSlot, laundryService, totalAmount);
            return ResponseEntity.status(HttpStatus.CREATED).body(booking);
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
                                                    @RequestBody BookingStatus status) {
            return ResponseEntity.ok(bookingService.updateStatus(id, status));
        }

        @PutMapping("/{id}/cancel")
        public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
            return ResponseEntity.ok(bookingService.cancelBooking(id));
        }

        // ---------- DELETE ----------

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        }

        // ---------- Helpers ----------

        private NoSuchElementException notFound(String entityName, Long id) {
            return new NoSuchElementException(entityName + " not found with id: " + id);
        }
    }

