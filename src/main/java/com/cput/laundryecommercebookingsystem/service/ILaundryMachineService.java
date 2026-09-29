package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;

import java.util.List;
import java.util.Optional;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */

public interface ILaundryMachineService {
    LaundryMachine createMachine(
            String machineNumber,
            String type,
            MachineStatus status,
            Long laundryRoomId
    );

    LaundryMachine updateMachineStatus(
            Long machineId,
            MachineStatus status
    );

    Optional<LaundryMachine> getMachineById(
            Long machineId
    );

    Optional<LaundryMachine> getMachineByNumber(
            String machineNumber
    );

    List<LaundryMachine> getMachinesByStatus(
            MachineStatus status
    );

    List<LaundryMachine> getAllMachines();

    void deleteMachine(
            Long machineId
    );
}

