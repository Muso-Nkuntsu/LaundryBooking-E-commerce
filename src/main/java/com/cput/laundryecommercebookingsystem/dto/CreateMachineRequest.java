package com.cput.laundryecommercebookingsystem.dto;

import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;

/** Body of POST /laundrymachine/create. */
public record CreateMachineRequest(String machineNumber,
                                   String type, MachineStatus status,
                                   Long laundryRoomId) {
}
