package com.cput.laundryecommercebookingsystem.service;
import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;

import java.util.List;
import java.util.Optional;
/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

public interface ILaundryRoomService {

    LaundryRoom createRoom(String roomNumber, String location, int capacity, String description);

    LaundryRoom activateRoom(Long roomId);

    LaundryRoom deactivateRoom(Long roomId);

    LaundryRoom updateRoom(Long roomId, String location, int capacity, String description);

    LaundryRoom addMachineToRoom(Long roomId, LaundryMachine machine);

    Optional<LaundryRoom> getRoomById(Long roomId);

    List<LaundryRoom> getActiveRooms();

    List<LaundryRoom> getAllRooms();

    boolean deleteRoom(Long roomId);
}
