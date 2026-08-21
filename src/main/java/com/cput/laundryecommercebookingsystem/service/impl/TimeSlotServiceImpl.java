package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.factory.TimeSlotFactory;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import com.cput.laundryecommercebookingsystem.service.TimeSlotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

// Libolwetu Nokenke 222665963
@Service
public class TimeSlotServiceImpl implements TimeSlotService {
    private final TimeSlotRepository timeSlotRepository;
    public TimeSlotServiceImpl(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    @Transactional
    public TimeSlot createTimeSlot(LocalTime startTime, LocalTime endTime, LocalDate date) {
        TimeSlot timeSlot = TimeSlotFactory.createTimeSlot(startTime, endTime, date, true);
        return timeSlotRepository.save(timeSlot);
    }
    @Override
    @Transactional
    public TimeSlot markAsBooked(Long timeSlotId) {
        TimeSlot existing = getTimeSlotOrThrow(timeSlotId);
        TimeSlot updated = rebuildWithAvailability(existing, false);
        return timeSlotRepository.save(updated);
    }
    @Override
    @Transactional
    public TimeSlot markAsAvailable(Long timeSlotId) {
        TimeSlot existing = getTimeSlotOrThrow(timeSlotId);
        TimeSlot updated = rebuildWithAvailability(existing, true);
        return timeSlotRepository.save(updated);
    }
    @Override
    @Transactional(readOnly = true)
    public Optional<TimeSlot> getTimeSlotById(Long timeSlotId) {
        return timeSlotRepository.findById(timeSlotId);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getAvailableTimeSlots() {
        return timeSlotRepository.findByIsAvailable(true);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getTimeSlotsByDate(LocalDate date) {
        return timeSlotRepository.findByDate(date);
    }
    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> getAllTimeSlots() {
        return timeSlotRepository.findAll();
    }
    @Override
    @Transactional
    public boolean deleteTimeSlot(Long timeSlotId) {
        if (timeSlotRepository.existsById(timeSlotId)) {
            timeSlotRepository.deleteById(timeSlotId);
            return true;
        }
        return false;
    }
    private TimeSlot getTimeSlotOrThrow(Long timeSlotId) {
        return timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new NoSuchElementException("TimeSlot not found with id: " + timeSlotId));
    }
    private TimeSlot rebuildWithAvailability(TimeSlot existing, boolean isAvailable) {
        return TimeSlot.builder()
                .setId(existing.getId())
                .setStartTime(existing.getStartTime())
                .setEndTime(existing.getEndTime())
                .setDate(existing.getDate())
                .setAvailable(isAvailable)
                .build();
    }
}

