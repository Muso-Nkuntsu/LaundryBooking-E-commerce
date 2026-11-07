package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminBookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/*AdminBookingController.java
 * Admin only: all laundry bookings, their status, and the day schedule.
 */
@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookingController {

    private final AdminBookingService bookingService;

    public AdminBookingController(AdminBookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** Example: GET /api/admin/bookings?status=PENDING&from=2026-11-01&to=2026-11-30&q=washer */
    @GetMapping
    public ResponseEntity<List<AdminBookingView>> list(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(bookingService.list(status, machineId, from, to, q));
    }

    /** Every machine against every time slot for one day. Example: GET /api/admin/bookings/schedule?date=2026-11-10 */
    @GetMapping("/schedule")
    public ResponseEntity<ScheduleView> schedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(bookingService.schedule(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminBookingView> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.get(id));
    }

    /** Body: { "status": "CONFIRMED" }. Allowed: PENDING to CONFIRMED or CANCELLED; CONFIRMED to COMPLETED or CANCELLED. */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AdminBookingView> updateStatus(@PathVariable Long id, @RequestBody BookingStatusRequest request) {
        return ResponseEntity.ok(bookingService.updateStatus(id, request == null ? null : request.status()));
    }
}
