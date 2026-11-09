package com.cput.laundryecommercebookingsystem.domain;

import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Lindokuhle Nanto
 * 240443608
 * 25 July 2026
 */


@Entity
@Table(name = "laundry_machine")
public class LaundryMachine {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long machineId;
    private String machineNumber;
    private String type;
    @Enumerated(EnumType.STRING)
    private MachineStatus status;


    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private LaundryRoom laundryRoom;
    protected LaundryMachine() {
    }


    private LaundryMachine(Builder builder) {
        this.machineNumber = builder.machineNumber;
        this.type = builder.type;
        this.status = builder.status;
        this.laundryRoom = builder.laundryRoom;
    }


    public static Builder builder() {
        return new Builder();
    }


    public Long getMachineId() {
        return machineId;
    }


    public String getMachineNumber() {
        return machineNumber;
    }


    public String getType() {
        return type;
    }


    public MachineStatus getStatus() {
        return status;
    }


    public LaundryRoom getLaundryRoom() {
        return laundryRoom;
    }


    /** Used by the admin dashboard to rename a machine, change its type or move it to another room. */
    public void updateDetails(String machineNumber, String type, LaundryRoom laundryRoom) {
        if (machineNumber == null || machineNumber.isBlank()) {
            throw new IllegalArgumentException("Machine number is required");
        }
        if (laundryRoom == null) {
            throw new IllegalArgumentException("Laundry room is required");
        }
        this.machineNumber = machineNumber.trim();
        this.type = type;
        this.laundryRoom = laundryRoom;
    }

    public void updateStatus(MachineStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Machine status cannot be null"
            );
        }

        this.status = status;
    }


    public static class Builder {

        private String machineNumber;
        private String type;
        private MachineStatus status;
        private LaundryRoom laundryRoom;


        public Builder machineNumber(String machineNumber) {
            this.machineNumber = machineNumber;
            return this;
        }


        public Builder type(String type) {
            this.type = type;
            return this;
        }


        public Builder status(MachineStatus status) {
            this.status = status;
            return this;
        }


        public Builder laundryRoom(LaundryRoom laundryRoom) {
            this.laundryRoom = laundryRoom;
            return this;
        }


        public LaundryMachine build() {

            if (machineNumber == null || machineNumber.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Machine number is required"
                );
            }

            if (type == null || type.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Machine type is required"
                );
            }

            if (status == null) {
                throw new IllegalArgumentException(
                        "Machine status is required"
                );
            }

            if (laundryRoom == null) {
                throw new IllegalArgumentException(
                        "Laundry room is required"
                );
            }

            return new LaundryMachine(this);
        }
    }
}