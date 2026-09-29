package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;

/**
 * Lindokuhle Nanto
 * 240443608
 * 25 July 2026
 */

public class LaundryMachineFactory {
    public LaundryMachine create(
            String machineNumber,
            String type,
            MachineStatus status,
            LaundryRoom laundryRoom) {

        return LaundryMachine.builder()
                .machineNumber(machineNumber)
                .type(type)
                .status(status)
                .laundryRoom(laundryRoom)
                .build();
    }
}