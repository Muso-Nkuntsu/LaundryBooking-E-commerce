package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.dto.CreateMachineRequest;
import com.cput.laundryecommercebookingsystem.service.ILaundryMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * LaundryMachineController.java
 *
 * Lindokuhle Nanto
 * 240443608
 * 29 July 2026
 */

@RestController
@RequestMapping("/laundrymachine")
public class LaundryMachineController {

    private final ILaundryMachineService laundryMachineService;

    @Autowired
    public LaundryMachineController(ILaundryMachineService laundryMachineService) {
        this.laundryMachineService = laundryMachineService;
    }

    /**
     * Example body:
     * { "machineNumber": "W-01", "type": "Washer", "status": "AVAILABLE", "laundryRoomId": 1 }
     */
    @PostMapping("/create")
    public ResponseEntity<LaundryMachine> createMachine(@RequestBody CreateMachineRequest request) {
        LaundryMachine createdMachine = laundryMachineService.createMachine(
                request.machineNumber(), request.type(), request.status(), request.laundryRoomId());
        return new ResponseEntity<>(createdMachine, HttpStatus.CREATED);
    }

    /** Machines that can still be booked for a time slot: GET /laundrymachine/available?timeSlotId=3 */
    @GetMapping("/available")
    public ResponseEntity<List<LaundryMachine>> getAvailableMachines(@RequestParam Long timeSlotId) {
        return ResponseEntity.ok(laundryMachineService.getAvailableMachines(timeSlotId));
    }

    @PatchMapping("/update-status/{machineId}")
    public ResponseEntity<LaundryMachine> updateMachineStatus(
            @PathVariable Long machineId,
            @RequestBody MachineStatus status) {
        try {
            LaundryMachine updatedMachine = laundryMachineService.updateMachineStatus(machineId, status);
            return ResponseEntity.ok(updatedMachine);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/read/{machineId}")
    public ResponseEntity<LaundryMachine> getMachineById(@PathVariable Long machineId) {
        return laundryMachineService.getMachineById(machineId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{machineNumber}")
    public ResponseEntity<LaundryMachine> getMachineByNumber(@PathVariable String machineNumber) {
        return laundryMachineService.getMachineByNumber(machineNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LaundryMachine>> getMachinesByStatus(@PathVariable MachineStatus status) {
        List<LaundryMachine> machines = laundryMachineService.getMachinesByStatus(status);
        return ResponseEntity.ok(machines);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<LaundryMachine>> getAllMachines() {
        List<LaundryMachine> machines = laundryMachineService.getAllMachines();
        return ResponseEntity.ok(machines);
    }

    @DeleteMapping("/delete/{machineId}")
    public ResponseEntity<Void> deleteMachine(@PathVariable Long machineId) {
        try {
            laundryMachineService.deleteMachine(machineId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}