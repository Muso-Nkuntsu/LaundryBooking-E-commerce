package com.cput.laundryecommercebookingsystem.controller;
/*
 * Muso Nkuntsu
 * 231223722
 * 29 July 2026
 */


import com.cput.laundryecommercebookingsystem.domain.*;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryServiceRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @WebMvcTest(BookingController.class)
    class BookingControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private IBookingService bookingService;

        @MockBean
        private IStudentRepository studentRepository;

        @MockBean
        private ILaundryMachineRepository laundryMachineRepository;

        @MockBean
        private TimeSlotRepository timeSlotRepository;

        @MockBean
        private ILaundryServiceRepository laundryServiceRepository;

        private Student student;
        private LaundryMachine machine;
        private TimeSlot timeSlot;
        private Booking booking;

        @BeforeEach
        void setUp() {
            student = mock(Student.class);
            machine = mock(LaundryMachine.class);
            timeSlot = mock(TimeSlot.class);

            booking = Booking.builder()
                    .student(student)
                    .laundryMachine(machine)
                    .timeSlot(timeSlot)
                    .bookingDate(LocalDateTime.now())
                    .status(BookingStatus.CONFIRMED)
                    .totalAmount(50.0)
                    .build();
        }

        // ---------- CREATE ----------

        @Test
        void createBooking_validRequest_returns201() throws Exception {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(laundryMachineRepository.findById(2L)).thenReturn(Optional.of(machine));
            when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(timeSlot));
            when(bookingService.createBooking(any(), any(), any(), any(), anyDouble()))
                    .thenReturn(booking);

            mockMvc.perform(post("/api/bookings")
                            .param("studentId", "1")
                            .param("machineId", "2")
                            .param("timeSlotId", "3")
                            .param("totalAmount", "50.0"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.totalAmount").value(50.0));
        }

        @Test
        void createBooking_unknownStudent_returns404() throws Exception {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(post("/api/bookings")
                            .param("studentId", "99")
                            .param("machineId", "2")
                            .param("timeSlotId", "3")
                            .param("totalAmount", "50.0"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void createBooking_conflictingSlot_returns409() throws Exception {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(laundryMachineRepository.findById(2L)).thenReturn(Optional.of(machine));
            when(timeSlotRepository.findById(3L)).thenReturn(Optional.of(timeSlot));
            when(bookingService.createBooking(any(), any(), any(), any(), anyDouble()))
                    .thenThrow(new IllegalStateException("This machine is already booked for the selected time slot."));

            mockMvc.perform(post("/api/bookings")
                            .param("studentId", "1")
                            .param("machineId", "2")
                            .param("timeSlotId", "3")
                            .param("totalAmount", "50.0"))
                    .andExpect(status().isConflict());
        }

        @Test
        void createBooking_missingRequiredParam_returns400() throws Exception {
            mockMvc.perform(post("/api/bookings")
                            .param("machineId", "2")
                            .param("timeSlotId", "3")
                            .param("totalAmount", "50.0"))
                    // studentId intentionally omitted — Spring rejects missing required @RequestParam
                    .andExpect(status().isBadRequest());
        }

        // ---------- READ ----------

        @Test
        void getAllBookings_returnsList() throws Exception {
            when(bookingService.findAll()).thenReturn(List.of(booking));

            mockMvc.perform(get("/api/bookings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
        }

        @Test
        void getBookingById_existingId_returnsBooking() throws Exception {
            when(bookingService.findById(1L)).thenReturn(Optional.of(booking));

            mockMvc.perform(get("/api/bookings/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        void getBookingById_unknownId_returns404() throws Exception {
            when(bookingService.findById(99L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/bookings/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void getBookingsByStudent_returnsFilteredList() throws Exception {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(bookingService.findByStudent(student)).thenReturn(List.of(booking));

            mockMvc.perform(get("/api/bookings/student/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"));
        }

        // ---------- UPDATE ----------

        @Test
        void updateStatus_validRequest_returnsUpdatedBooking() throws Exception {
            Booking updated = Booking.builder()
                    .student(student)
                    .laundryMachine(machine)
                    .timeSlot(timeSlot)
                    .bookingDate(LocalDateTime.now())
                    .status(BookingStatus.COMPLETED)
                    .totalAmount(50.0)
                    .build();

            when(bookingService.updateStatus(1L, BookingStatus.COMPLETED)).thenReturn(updated);

            mockMvc.perform(put("/api/bookings/1/status")
                            .param("status", "COMPLETED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }

        @Test
        void cancelBooking_existingId_returnsCancelledBooking() throws Exception {
            Booking cancelled = Booking.builder()
                    .student(student)
                    .laundryMachine(machine)
                    .timeSlot(timeSlot)
                    .bookingDate(LocalDateTime.now())
                    .status(BookingStatus.CANCELLED)
                    .totalAmount(50.0)
                    .build();

            when(bookingService.cancelBooking(1L)).thenReturn(cancelled);

            mockMvc.perform(put("/api/bookings/1/cancel"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));
        }

        @Test
        void cancelBooking_unknownId_returns404() throws Exception {
            when(bookingService.cancelBooking(99L))
                    .thenThrow(new IllegalArgumentException("No booking found with id: 99"));

            mockMvc.perform(put("/api/bookings/99/cancel"))
                    .andExpect(status().isNotFound());
        }

        // ---------- DELETE ----------

        @Test
        void deleteBooking_existingId_returns204() throws Exception {
            doNothing().when(bookingService).deleteBooking(1L);

            mockMvc.perform(delete("/api/bookings/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        void deleteBooking_unknownId_returns404() throws Exception {
            doThrow(new IllegalArgumentException("No booking found with id: 99"))
                    .when(bookingService).deleteBooking(99L);

            mockMvc.perform(delete("/api/bookings/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
