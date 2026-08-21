package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.service.TimeSlotService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
@RestController
@RequestMapping("/timeslot")
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    public TimeSlotController(TimeSlotService timeSlotService) {
        this.timeSlotService = timeSlotService;
    }
    @PostMapping("/create")
    public ResponseEntity<TimeSlot> createTimeSlot(@RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                                   @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        TimeSlot timeSlot = timeSlotService.createTimeSlot(startTime, endTime, date);
        return ResponseEntity.ok(timeSlot);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TimeSlot>> getAllTimeSlots() {
        return ResponseEntity.ok(timeSlotService.getAllTimeSlots());
    }
    @GetMapping("/{id}")
    public ResponseEntity<TimeSlot> getTimeSlotById(@PathVariable Long id) {
        Optional<TimeSlot> timeSlot = timeSlotService.getTimeSlotById(id);
        if (timeSlot.isPresent()) {
            return ResponseEntity.ok(timeSlot.get());
        }
        return ResponseEntity.notFound().build();
    }
    @PutMapping("/{id}/update")
    public ResponseEntity<TimeSlot> updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        try {
            TimeSlot updated = available ? timeSlotService.markAsAvailable(id) : timeSlotService.markAsBooked(id);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeSlot(@PathVariable Long id) {
        if (timeSlotService.deleteTimeSlot(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
