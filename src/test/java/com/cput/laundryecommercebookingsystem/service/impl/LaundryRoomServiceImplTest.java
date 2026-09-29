package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.repository.ILaundryRoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

@ExtendWith(MockitoExtension.class)
class LaundryRoomServiceImplTest {

    @Mock
    private ILaundryRoomRepository laundryRoomRepository;

    private LaundryRoomServiceImpl laundryRoomService;

    @BeforeEach
    void setUp() {
        laundryRoomService = new LaundryRoomServiceImpl(laundryRoomRepository);
    }

    private LaundryRoom buildRoom(int roomId, String roomNumber, int capacity, boolean active) {
        return new LaundryRoom.Builder()
                .roomId(roomId)
                .roomNumber(roomNumber)
                .location("Main Residence")
                .capacity(capacity)
                .description("Test room")
                .isActive(active)
                .build();
    }

    @Test
    void createRoom_delegatesToFactoryAndSaves() {
        when(laundryRoomRepository.save(any(LaundryRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LaundryRoom result = laundryRoomService.createRoom("Room 4B", "Main Residence", 6, "Standard room");

        assertEquals("Room 4B", result.getRoomNumber());
        assertEquals(6, result.getCapacity());
        assertFalse(result.isActive());
    }

    @Test
    void activateRoom_withExistingInactiveRoom_activatesIt() {
        LaundryRoom room = buildRoom(1, "Room 1A", 4, false);
        when(laundryRoomRepository.findById(1)).thenReturn(Optional.of(room));
        when(laundryRoomRepository.save(any(LaundryRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LaundryRoom result = laundryRoomService.activateRoom(1);

        assertTrue(result.isActive());
    }

    @Test
    void activateRoom_withNonExistentId_throwsNoSuchElementException() {
        when(laundryRoomRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> laundryRoomService.activateRoom(999));
        org.mockito.Mockito.verify(laundryRoomRepository, never()).save(any());
    }

    @Test
    void activateRoom_withAlreadyActiveRoom_throwsIllegalStateException() {
        LaundryRoom room = buildRoom(2, "Room 1B", 4, true);
        when(laundryRoomRepository.findById(2)).thenReturn(Optional.of(room));

        assertThrows(IllegalStateException.class, () -> laundryRoomService.activateRoom(2));
    }

    @Test
    void deactivateRoom_withExistingActiveRoom_deactivatesIt() {
        LaundryRoom room = buildRoom(3, "Room 1C", 4, true);
        when(laundryRoomRepository.findById(3)).thenReturn(Optional.of(room));
        when(laundryRoomRepository.save(any(LaundryRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LaundryRoom result = laundryRoomService.deactivateRoom(3);

        assertFalse(result.isActive());
    }

    @Test
    void updateRoom_withValidInputs_updatesDetails() {
        LaundryRoom room = buildRoom(4, "Room 2A", 4, false);
        when(laundryRoomRepository.findById(4)).thenReturn(Optional.of(room));
        when(laundryRoomRepository.save(any(LaundryRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LaundryRoom result = laundryRoomService.updateRoom(4, "New Location", 8, "Updated description");

        assertEquals("New Location", result.getLocation());
        assertEquals(8, result.getCapacity());
        assertEquals("Updated description", result.getDescription());
    }

    @Test
    void updateRoom_withNonExistentId_throwsNoSuchElementException() {
        when(laundryRoomRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> laundryRoomService.updateRoom(999, "Loc", 4, "desc"));
    }

    @Test
    void addMachineToRoom_withCapacityAvailable_addsMachine() {
        LaundryRoom room = buildRoom(5, "Room 3A", 2, false);
        LaundryMachine machine = new LaundryMachine(1);
        when(laundryRoomRepository.findById(5)).thenReturn(Optional.of(room));
        when(laundryRoomRepository.save(any(LaundryRoom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LaundryRoom result = laundryRoomService.addMachineToRoom(5, machine);

        assertEquals(1, result.getMachines().size());
        assertEquals(room, machine.getRoom());
    }

    @Test
    void addMachineToRoom_atFullCapacity_throwsIllegalStateException() {
        LaundryRoom room = buildRoom(6, "Room 3B", 1, false);
        room.addMachine(new Machine(1));
        when(laundryRoomRepository.findById(6)).thenReturn(Optional.of(room));

        assertThrows(IllegalStateException.class,
                () -> laundryRoomService.addMachineToRoom(6, new Machine(2)));
    }

    @Test
    void getRoomById_delegatesToRepository() {
        LaundryRoom room = buildRoom(7, "Room 4A", 4, true);
        when(laundryRoomRepository.findById(7)).thenReturn(Optional.of(room));

        Optional<LaundryRoom> result = laundryRoomService.getRoomById(7);

        assertEquals(room, result.orElseThrow());
    }

    @Test
    void getActiveRooms_delegatesToRepository() {
        List<LaundryRoom> activeRooms = List.of(buildRoom(8, "Room 5A", 4, true));
        when(laundryRoomRepository.findByIsActive(true)).thenReturn(activeRooms);

        List<LaundryRoom> result = laundryRoomService.getActiveRooms();

        assertEquals(activeRooms, result);
    }

    @Test
    void getAllRooms_delegatesToRepository() {
        List<LaundryRoom> allRooms = List.of(buildRoom(9, "Room 6A", 4, false), buildRoom(10, "Room 6B", 4, true));
        when(laundryRoomRepository.findAll()).thenReturn(allRooms);

        List<LaundryRoom> result = laundryRoomService.getAllRooms();

        assertEquals(allRooms, result);
    }

}
