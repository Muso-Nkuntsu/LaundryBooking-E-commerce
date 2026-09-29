package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.service.ILaundryRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

@RestController
@RequestMapping("/laundry-room")
@CrossOrigin(origins = "*")
public class LaundryRoomController {

    private final ILaundryRoomService service;

    public LaundryRoomController(ILaundryRoomService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ResponseEntity<LaundryRoom> createRoom(
            @RequestParam String roomNumber,
            @RequestParam String location,
            @RequestParam int capacity,
            @RequestParam String description) {

        return ResponseEntity.ok(
                service.createRoom(roomNumber, location, capacity, description)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaundryRoom> getRoom(@PathVariable int id) {
        return service.getRoomById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<LaundryRoom>> getAllRooms() {
        return ResponseEntity.ok(service.getAllRooms());
    }

    @GetMapping("/active")
    public ResponseEntity<List<LaundryRoom>> getActiveRooms() {
        return ResponseEntity.ok(service.getActiveRooms());
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<LaundryRoom> updateRoom(
            @PathVariable int id,
            @RequestParam String location,
            @RequestParam int capacity,
            @RequestParam String description) {

        return ResponseEntity.ok(
                service.updateRoom(id, location, capacity, description)
        );
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<LaundryRoom> activateRoom(@PathVariable int id) {
        return ResponseEntity.ok(service.activateRoom(id));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<LaundryRoom> deactivateRoom(@PathVariable int id) {
        return ResponseEntity.ok(service.deactivateRoom(id));
    }

    @PostMapping("/{id}/machines")
    public ResponseEntity<LaundryRoom> addMachine(
            @PathVariable int id,
            @RequestBody LaundryMachine machine) {

        return ResponseEntity.ok(
                service.addMachineToRoom(id, machine)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable int id) {
        boolean deleted = service.deleteRoom(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
}