package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.repository.ILaundryMachineRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * LaundryMachineServiceImplTest.java
 *
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */

@ExtendWith(MockitoExtension.class)
public class LaundryMachineServiceImplTest {

    @Mock
    private ILaundryMachineRepository laundryMachineRepository;

    @Mock
    private ILaundryRoomRepository laundryRoomRepository;

    @InjectMocks
    private LaundryMachineServiceImpl laundryMachineService;

    private LaundryMachine laundryMachine;
    private LaundryRoom laundryRoom;

    @BeforeEach
    void setUp() {

        laundryRoom = mock(LaundryRoom.class);

        laundryMachine = LaundryMachine.builder()
                .machineNumber("WM-001")
                .type("WASHER")
                .status(MachineStatus.AVAILABLE)
                .laundryRoom(laundryRoom)
                .build();
    }

    @Test
    void createMachine_returnsSavedLaundryMachine() {

        when(laundryRoomRepository.findById(1))
                .thenReturn(Optional.of(laundryRoom));

        when(laundryMachineRepository.save(
                any(LaundryMachine.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        LaundryMachine result =
                laundryMachineService.createMachine(
                        "WM-001",
                        "WASHER",
                        MachineStatus.AVAILABLE,
                        1L
                );

        assertNotNull(result);

        assertEquals(
                "WM-001",
                result.getMachineNumber()
        );

        assertEquals(
                "WASHER",
                result.getType()
        );

        assertEquals(
                MachineStatus.AVAILABLE,
                result.getStatus()
        );

        assertEquals(
                laundryRoom,
                result.getLaundryRoom()
        );

        verify(laundryRoomRepository)
                .findById(1);

        verify(laundryMachineRepository)
                .save(any(LaundryMachine.class));
    }

    @Test
    void createMachine_throwsExceptionWhenLaundryRoomNotFound() {

        when(laundryRoomRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> laundryMachineService.createMachine(
                        "WM-001",
                        "WASHER",
                        MachineStatus.AVAILABLE,
                        1L
                )
        );

        verify(laundryRoomRepository)
                .findById(1);

        verify(laundryMachineRepository, never())
                .save(any(LaundryMachine.class));
    }

    @Test
    void getMachineById_returnsLaundryMachine() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.of(laundryMachine));

        Optional<LaundryMachine> result =
                laundryMachineService.getMachineById(1L);

        assertTrue(result.isPresent());

        assertEquals(
                "WM-001",
                result.get().getMachineNumber()
        );

        verify(laundryMachineRepository)
                .findById(1L);
    }

    @Test
    void getMachineById_returnsEmptyWhenMachineNotFound() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.empty());

        Optional<LaundryMachine> result =
                laundryMachineService.getMachineById(1L);

        assertTrue(result.isEmpty());

        verify(laundryMachineRepository)
                .findById(1L);
    }

    @Test
    void getMachineByNumber_returnsLaundryMachine() {

        when(laundryMachineRepository
                .findByMachineNumber("WM-001"))
                .thenReturn(Optional.of(laundryMachine));

        Optional<LaundryMachine> result =
                laundryMachineService
                        .getMachineByNumber("WM-001");

        assertTrue(result.isPresent());

        assertEquals(
                "WM-001",
                result.get().getMachineNumber()
        );

        verify(laundryMachineRepository)
                .findByMachineNumber("WM-001");
    }

    @Test
    void getMachinesByStatus_returnsList() {

        when(laundryMachineRepository
                .findByStatus(MachineStatus.AVAILABLE))
                .thenReturn(List.of(laundryMachine));

        List<LaundryMachine> result =
                laundryMachineService
                        .getMachinesByStatus(
                                MachineStatus.AVAILABLE
                        );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                MachineStatus.AVAILABLE,
                result.get(0).getStatus()
        );

        verify(laundryMachineRepository)
                .findByStatus(MachineStatus.AVAILABLE);
    }

    @Test
    void getAllMachines_returnsList() {

        when(laundryMachineRepository.findAll())
                .thenReturn(List.of(laundryMachine));

        List<LaundryMachine> result =
                laundryMachineService.getAllMachines();

        assertEquals(
                1,
                result.size()
        );

        verify(laundryMachineRepository)
                .findAll();
    }

    @Test
    void updateMachineStatus_returnsUpdatedMachine() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.of(laundryMachine));

        when(laundryMachineRepository.save(
                any(LaundryMachine.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        LaundryMachine result =
                laundryMachineService.updateMachineStatus(
                        1L,
                        MachineStatus.IN_USE
                );

        assertNotNull(result);

        assertEquals(
                MachineStatus.IN_USE,
                result.getStatus()
        );

        verify(laundryMachineRepository)
                .findById(1L);

        verify(laundryMachineRepository)
                .save(laundryMachine);
    }

    @Test
    void updateMachineStatus_throwsExceptionWhenMachineNotFound() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> laundryMachineService.updateMachineStatus(
                        1L,
                        MachineStatus.IN_USE
                )
        );

        verify(laundryMachineRepository)
                .findById(1L);

        verify(laundryMachineRepository, never())
                .save(any(LaundryMachine.class));
    }

    @Test
    void deleteMachine_callsRepository() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.of(laundryMachine));

        laundryMachineService.deleteMachine(1L);

        verify(laundryMachineRepository)
                .findById(1L);

        verify(laundryMachineRepository)
                .delete(laundryMachine);
    }

    @Test
    void deleteMachine_throwsExceptionWhenMachineNotFound() {

        when(laundryMachineRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> laundryMachineService.deleteMachine(1L)
        );

        verify(laundryMachineRepository)
                .findById(1L);

        verify(laundryMachineRepository, never())
                .delete(any(LaundryMachine.class));
    }
}