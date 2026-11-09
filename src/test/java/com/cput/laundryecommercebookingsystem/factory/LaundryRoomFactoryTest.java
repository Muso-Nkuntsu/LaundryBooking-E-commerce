package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */

class LaundryRoomFactoryTest {

    @Test
    void createLaundryRoom_withValidInputs_returnsRoomWithExpectedValues() {
        LaundryRoom room = LaundryRoomFactory.createLaundryRoom(
                "Room 4B", "Main Residence, Ground Floor", 6, "Standard laundry room");

        assertNotNull(room);
        assertEquals("Room 4B", room.getRoomNumber());
        assertEquals("Main Residence, Ground Floor", room.getLocation());
        assertEquals(6, room.getCapacity());
        assertEquals("Standard laundry room", room.getDescription());
        assertTrue(room.getMachines().isEmpty());
    }

    @Test
    void createLaundryRoom_defaultOverload_isInactiveByDefault() {
        LaundryRoom room = LaundryRoomFactory.createLaundryRoom(
                "Room 1A", "West Wing", 4, "New room");

        assertFalse(room.isActive());
    }

    @Test
    void createLaundryRoom_withMachinesWithinCapacity_relationshipIsCorrectlySet() {
        List<LaundryMachine> machines = List.of(new LaundryMachine(1), new LaundryMachine(2));

        LaundryRoom room = LaundryRoomFactory.createLaundryRoom(
                "Room 2C", "East Wing", 4, "Has machines", machines);

        assertEquals(2, room.getMachines().size());

        for (LaundryMachine machine : room.getMachines()) {
            assertEquals(room, machine.getRoom());
        }
    }

    @Test
    void createLaundryRoom_withZeroCapacityAndNoMachines_isAllowed() {
        LaundryRoom room = LaundryRoomFactory.createLaundryRoom(
                "Room 5D", "Basement", 0, "Not yet equipped");

        assertEquals(0, room.getCapacity());
        assertTrue(room.getMachines().isEmpty());
    }

    @Test
    void createLaundryRoom_withNullRoomNumber_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom(null, "Location", 4, "desc"));
    }

    @Test
    void createLaundryRoom_withBlankRoomNumber_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("   ", "Location", 4, "desc"));
    }

    @Test
    void createLaundryRoom_withNullLocation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("Room 1", null, 4, "desc"));
    }

    @Test
    void createLaundryRoom_withBlankLocation_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("Room 1", "   ", 4, "desc"));
    }

    @Test
    void createLaundryRoom_withNegativeCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("Room 1", "Location", -1, "desc"));
    }

    @Test
    void createLaundryRoom_withNullMachinesList_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("Room 1", "Location", 4, "desc", null));
    }

    @Test
    void createLaundryRoom_withMachinesExceedingCapacity_throwsIllegalArgumentException() {
        List<LaundryMachine> machines = List.of(new LaundryMachine(1), new LaundryMachine(2), new LaundryMachine(3));

        assertThrows(IllegalArgumentException.class,
                () -> LaundryRoomFactory.createLaundryRoom("Room 1", "Location", 2, "desc", machines));
    }
}
