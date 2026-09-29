package com.cput.laundryecommercebookingsystem.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */
@Entity
@Table(name = "laundry_rooms")
public class LaundryRoom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false, updatable = false)
    private int roomId;


    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;


    @Column(name = "location", nullable = false, length = 255)
    private String location;


    @Column(name = "capacity", nullable = false)
    private int capacity;


    @Column(name = "description", length = 500)
    private String description;


    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @OneToMany(mappedBy = "laundryRoom", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LaundryMachine> machines = new ArrayList<>();

    protected LaundryRoom() {
    }

    private LaundryRoom(Builder builder) {
        this.roomId = builder.roomId;
        this.roomNumber = builder.roomNumber;
        this.location = builder.location;
        this.capacity = builder.capacity;
        this.description = builder.description;
        this.isActive = builder.isActive;
        this.machines = new ArrayList<>(builder.machines);

    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getLocation() {
        return location;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return isActive;
    }

    public List<LaundryMachine> getMachines() {
        return Collections.unmodifiableList(machines);
    }


    public void addRoom() {
        if (this.isActive) {
            throw new IllegalStateException("Room is already active.");
        }
        if (this.machines.size() > this.capacity) {
            throw new IllegalStateException(
                    "Cannot activate room: machine count exceeds capacity.");
        }
        this.isActive = true;
    }

    public void updateRoom(String location, int capacity, String description) {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException("Location must not be blank.");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must not be negative.");
        }
        this.location = location;
        this.capacity = capacity;
        this.description = description;
    }


    public void deactivateRoom() {
        this.isActive = false;
    }


    public void addMachine(LaundryMachine machine) {
        if (machine == null) {
            throw new IllegalArgumentException("Machine must not be null.");
        }
        if (this.machines.size() >= this.capacity) {
            throw new IllegalStateException("Room is at full capacity.");
        }

        this.machines.add(machine);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LaundryRoom)) return false;
        LaundryRoom room = (LaundryRoom) o;
        return roomId == room.roomId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }

    @Override
    public String toString() {
        return "LaundryRoom{" +
                "roomId=" + roomId +
                ", roomNumber='" + roomNumber + '\'' +
                ", location='" + location + '\'' +
                ", capacity=" + capacity +
                ", isActive=" + isActive +
                ", machineCount=" + machines.size() +
                '}';
    }

    public static class Builder {
        private int roomId;
        private String roomNumber;
        private String location;
        private int capacity;
        private String description;
        private boolean isActive = false;
        private List<LaundryMachine> machines = new ArrayList<>();

        public Builder roomId(int roomId) {
            this.roomId = roomId;
            return this;
        }

        public Builder roomNumber(String roomNumber) {
            this.roomNumber = roomNumber;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder capacity(int capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder machines(List<LaundryMachine> machines) {
            this.machines = new ArrayList<>(machines);
            return this;
        }

        public Builder addMachine(LaundryMachine machine) {
            this.machines.add(machine);
            return this;
        }


        public LaundryRoom build() {
            if (location == null || location.isBlank()) {
                throw new IllegalStateException("LaundryRoom must have a location.");
            }
            if (capacity < 0) {
                throw new IllegalStateException("Capacity must not be negative.");
            }
            if (machines.size() > capacity) {
                throw new IllegalStateException(
                        "Number of machines exceeds room capacity.");
            }
            return new LaundryRoom(this);
        }
    }
}