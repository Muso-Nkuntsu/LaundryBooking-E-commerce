package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */
public interface ILaundryMachineRepository extends JpaRepository<LaundryMachine, Long> {

    Optional<LaundryMachine> findByMachineNumber(String machineNumber);

    List<LaundryMachine> findByStatus(MachineStatus status);

    List<LaundryMachine> findByType(String type);
}
