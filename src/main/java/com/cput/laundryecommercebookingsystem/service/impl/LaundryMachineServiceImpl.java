package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.factory.LaundryMachineFactory;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.service.ILaundryMachineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */

@Service
public class LaundryMachineServiceImpl
        implements ILaundryMachineService {

    private final ILaundryMachineRepository laundryMachineRepository;

    public LaundryMachineServiceImpl(
            ILaundryMachineRepository laundryMachineRepository) {

        this.laundryMachineRepository =
                laundryMachineRepository;
    }

    @Override
    @Transactional
    public LaundryMachine createMachine(
            String machineNumber,
            String type,
            MachineStatus status,
            Long laundryRoomId) {


        throw new UnsupportedOperationException(
                "Implement LaundryRoom lookup before creating LaundryMachine."
        );
    }

    @Override
    @Transactional
    public LaundryMachine updateMachineStatus(
            Long machineId,
            MachineStatus status) {

        LaundryMachine machine =
                getMachineOrThrow(machineId);

        machine.updateStatus(status);

        return laundryMachineRepository.save(machine);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LaundryMachine> getMachineById(
            Long machineId) {

        return laundryMachineRepository.findById(machineId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LaundryMachine> getMachineByNumber(
            String machineNumber) {

        return laundryMachineRepository
                .findByMachineNumber(machineNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaundryMachine> getMachinesByStatus(
            MachineStatus status) {

        return laundryMachineRepository
                .findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaundryMachine> getAllMachines() {

        return laundryMachineRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteMachine(Long machineId) {

        LaundryMachine machine =
                getMachineOrThrow(machineId);

        laundryMachineRepository.delete(machine);
    }

    private LaundryMachine getMachineOrThrow(
            Long machineId) {

        return laundryMachineRepository.findById(machineId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "LaundryMachine not found with id: "
                                        + machineId
                        )
                );
    }
}
