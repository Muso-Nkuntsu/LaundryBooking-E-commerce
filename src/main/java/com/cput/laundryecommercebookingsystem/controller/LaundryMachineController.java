package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
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

    @PostMapping("/create")
    public ResponseEntity<LaundryMachine> createMachine(
            @RequestParam String machineNumber,
            @RequestParam String type,
            @RequestParam MachineStatus status,
            @RequestParam Long laundryRoomId) {
        try {
            LaundryMachine createdMachine = laundryMachineService.createMachine(
                    machineNumber, type, status, laundryRoomId);
            return new ResponseEntity<>(createdMachine, HttpStatus.CREATED);
        } catch (NoSuchElementException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/update-status/{machineId}")
    public ResponseEntity<LaundryMachine> updateMachineStatus(
            @PathVariable Long machineId,
            @RequestParam MachineStatus status) {
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