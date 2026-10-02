package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Booking;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.TimeSlot;
import com.cput.laundryecommercebookingsystem.domain.enums.BookingStatus;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.factory.LaundryMachineFactory;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryRoomRepository;
import com.cput.laundryecommercebookingsystem.repository.TimeSlotRepository;
import com.cput.laundryecommercebookingsystem.repository.iBookingRepository;
import com.cput.laundryecommercebookingsystem.service.ILaundryMachineService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final ILaundryRoomRepository laundryRoomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final iBookingRepository bookingRepository;

    public LaundryMachineServiceImpl(
            ILaundryMachineRepository laundryMachineRepository,
            ILaundryRoomRepository laundryRoomRepository,
            TimeSlotRepository timeSlotRepository,
            iBookingRepository bookingRepository) {

        this.laundryMachineRepository = laundryMachineRepository;
        this.laundryRoomRepository = laundryRoomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public LaundryMachine createMachine(
            String machineNumber,
            String type,
            MachineStatus status,
            Long laundryRoomId) {

        if (laundryRoomId == null) {
            throw new IllegalArgumentException("Laundry room ID is required");
        }

        LaundryRoom room = laundryRoomRepository.findById(laundryRoomId)
                .orElseThrow(() -> new NoSuchElementException(
                        "LaundryRoom not found with id: " + laundryRoomId));

        if (machineNumber != null
                && laundryMachineRepository.findByMachineNumber(machineNumber).isPresent()) {
            throw new IllegalStateException(
                    "A machine with number " + machineNumber + " already exists");
        }

        // New machines start as AVAILABLE unless another status is given.
        LaundryMachine machine = new LaundryMachineFactory().create(
                machineNumber,
                type,
                status != null ? status : MachineStatus.AVAILABLE,
                room);

        return laundryMachineRepository.save(machine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LaundryMachine> getAvailableMachines(Long timeSlotId) {

        TimeSlot timeSlot = timeSlotRepository.findById(timeSlotId)
                .orElseThrow(() -> new NoSuchElementException(
                        "TimeSlot not found with id: " + timeSlotId));

        // Machines that already have an active booking in this slot.
        Set<Long> bookedMachineIds = bookingRepository
                .findByTimeSlotAndStatusNot(timeSlot, BookingStatus.CANCELLED)
                .stream()
                .map(Booking::getLaundryMachine)
                .map(LaundryMachine::getMachineId)
                .collect(Collectors.toSet());

        // IN_USE describes right now, so it does not stop a booking for a later slot.
        return laundryMachineRepository.findAll()
                .stream()
                .filter(machine -> machine.getStatus() != MachineStatus.OUT_OF_ORDER)
                .filter(machine -> !bookedMachineIds.contains(machine.getMachineId()))
                .collect(Collectors.toList());
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
