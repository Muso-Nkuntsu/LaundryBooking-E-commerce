package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Libolwetu Nokenke 222665963

public interface TimeSlotService {
    TimeSlot createTimeSlot(LocalTime startTime, LocalTime endTime, LocalDate date);
    TimeSlot markAsBooked(Long timeSlotId);
    TimeSlot markAsAvailable(Long timeSlotId);
    Optional<TimeSlot> getTimeSlotById(Long timeSlotId);
    List<TimeSlot> getAvailableTimeSlots();
    List<TimeSlot> getTimeSlotsByDate(LocalDate date);
    List<TimeSlot> getAllTimeSlots();
    boolean deleteTimeSlot(Long timeSlotId);
}

