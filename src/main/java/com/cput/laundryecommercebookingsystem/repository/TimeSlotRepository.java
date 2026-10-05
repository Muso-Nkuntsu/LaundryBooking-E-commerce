package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Libolwetu Nokenke 222665963

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByDate(LocalDate date);
    List<TimeSlot> findByIsAvailable(boolean isAvailable);
    List<TimeSlot> findByDateBetweenOrderByDateAscStartTimeAsc(LocalDate from, LocalDate to);
}

