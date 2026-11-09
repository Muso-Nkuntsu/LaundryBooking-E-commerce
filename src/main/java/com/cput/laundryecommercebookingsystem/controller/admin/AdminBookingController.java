package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.service.IBookingService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final IBookingService bookingService;

    public AdminBookingController(IBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<BookingRow> list() {
        return bookingService.findAll().stream()
                .sorted(Comparator.comparing(Booking::getBookingDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(BookingRow::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public BookingRow get(@PathVariable Long id) {
        return BookingRow.from(bookingService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found.")));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public BookingRow updateStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusRequest request) {
        bookingService.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found."));
        return BookingRow.from(bookingService.updateStatus(id, BookingStatus.valueOf(request.status())));
    }
}
