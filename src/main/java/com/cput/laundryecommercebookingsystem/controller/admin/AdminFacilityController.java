package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminFacilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/*AdminFacilityController.java
 * Admin only: laundry rooms, machines and time slots.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFacilityController {

    private final AdminFacilityService facilityService;

    public AdminFacilityController(AdminFacilityService facilityService) {
        this.facilityService = facilityService;
    }

    // ---------- Rooms ----------

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomView>> rooms() {
        return ResponseEntity.ok(facilityService.listRooms());
    }

    /** Body: { "roomNumber": "A1", "location": "Block A ground floor", "capacity": 6, "description": "" } */
    @PostMapping("/rooms")
    public ResponseEntity<RoomView> createRoom(@RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.createRoom(request));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<RoomView> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        return ResponseEntity.ok(facilityService.updateRoom(id, request));
    }

    /** Body: { "active": false } */
    @PatchMapping("/rooms/{id}/active")
    public ResponseEntity<RoomView> setRoomActive(@PathVariable Long id, @RequestBody ActiveRequest request) {
        return ResponseEntity.ok(facilityService.setRoomActive(id, requireFlag(request == null ? null : request.active())));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        facilityService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Machines ----------

    @GetMapping("/machines")
    public ResponseEntity<List<MachineView>> machines() {
        return ResponseEntity.ok(facilityService.listMachines());
    }

    /** Body: { "machineNumber": "W-01", "type": "WASHER", "roomId": 1, "status": "AVAILABLE" } */
    @PostMapping("/machines")
    public ResponseEntity<MachineView> createMachine(@RequestBody MachineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.createMachine(request));
    }

    @PutMapping("/machines/{id}")
    public ResponseEntity<MachineView> updateMachine(@PathVariable Long id, @RequestBody MachineRequest request) {
        return ResponseEntity.ok(facilityService.updateMachine(id, request));
    }

    /** Body: { "status": "OUT_OF_ORDER" } */
    @PatchMapping("/machines/{id}/status")
    public ResponseEntity<MachineStatusResult> setMachineStatus(@PathVariable Long id,
                                                                @RequestBody MachineStatusRequest request) {
        return ResponseEntity.ok(facilityService.setMachineStatus(id, request == null ? null : request.status()));
    }

    @DeleteMapping("/machines/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long id) {
        facilityService.deleteMachine(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Time slots ----------

    /** Example: GET /api/admin/timeslots?from=2026-11-07&to=2026-11-20 (default: the next 14 days) */
    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlotView>> timeSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(facilityService.listSlots(from, to));
    }

    /** Body: { "date": "2026-11-10", "startTime": "08:00", "endTime": "09:00" } */
    @PostMapping("/timeslots")
    public ResponseEntity<TimeSlotView> createSlot(@RequestBody TimeSlotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facilityService.createSlot(request));
    }

    /** Body: { "fromDate": "2026-11-10", "toDate": "2026-11-16", "dayStart": "06:00", "dayEnd": "22:00", "slotMinutes": 60 } */
    @PostMapping("/timeslots/generate")
    public ResponseEntity<GenerateSlotsResult> generateSlots(@RequestBody GenerateSlotsRequest request) {
        return ResponseEntity.ok(facilityService.generateSlots(request));
    }

    /** Body: { "available": false } closes the slot so it can't be booked. */
    @PatchMapping("/timeslots/{id}/available")
    public ResponseEntity<TimeSlotView> setSlotAvailable(@PathVariable Long id, @RequestBody AvailableRequest request) {
        return ResponseEntity.ok(facilityService.setSlotAvailable(id, requireFlag(request == null ? null : request.available())));
    }

    @DeleteMapping("/timeslots/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        facilityService.deleteSlot(id);
        return ResponseEntity.noContent().build();
    }

    private static boolean requireFlag(Boolean value) {
        if (value == null) {
            throw new IllegalArgumentException("Send true or false");
        }
        return value;
    }
}
