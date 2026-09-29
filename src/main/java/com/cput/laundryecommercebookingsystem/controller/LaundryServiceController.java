package com.cput.laundryecommercebookingsystem.controller;

/*
 * LaundryServiceController.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.service.ILaundryService;

import java.util.List;

@RestController
@RequestMapping("/laundry-service")
public class LaundryServiceController {

    private final ILaundryService laundryService;

    @Autowired
    public LaundryServiceController(ILaundryService laundryService) {
        this.laundryService = laundryService;
    }

    @PostMapping("/create")
    public ResponseEntity<LaundryService> create(@RequestBody LaundryService service) {
        LaundryService created = laundryService.create(service);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<LaundryService> read(@PathVariable String id) {
        LaundryService service = laundryService.read(id);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service);
    }

    @PostMapping("/update")
    public ResponseEntity<LaundryService> update(@RequestBody LaundryService service) {
        LaundryService updated = laundryService.update(service);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable String id) {
        boolean deleted = laundryService.delete(id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<LaundryService>> getAll() {
        return ResponseEntity.ok(laundryService.getAll());
    }
}

