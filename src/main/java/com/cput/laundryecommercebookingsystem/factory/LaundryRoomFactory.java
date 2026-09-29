package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;


import java.util.ArrayList;
import java.util.List;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */

public final class LaundryRoomFactory {

    private LaundryRoomFactory() {

    }

    public static LaundryRoom createLaundryRoom(String roomNumber, String location, int capacity, String description) {
        return createLaundryRoom(roomNumber, location, capacity, description, new ArrayList<>());
    }


    public static LaundryRoom createLaundryRoom(String roomNumber,
                                                String location,
                                                int capacity,
                                                String description,
                                                List<LaundryMachine> machines) {

        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number must not be blank.");
        }
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location must not be blank.");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be negative.");
        }
        if (machines == null) {
            throw new IllegalArgumentException("Machines list must not be null (use an empty list instead).");
        }
        if (machines.size() > capacity) {
            throw new IllegalArgumentException("Number of machines exceeds room capacity.");
        }

        return new LaundryRoom.Builder()
                .roomNumber(roomNumber)
                .location(location)
                .capacity(capacity)
                .description(description)
                .isActive(false)
                .machines(machines)
                .build();
    }
}
