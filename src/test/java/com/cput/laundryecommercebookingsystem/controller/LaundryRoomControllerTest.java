package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import com.cput.laundryecommercebookingsystem.service.ILaundryRoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Talent Nocuze
 * 230405886
 * 25 July 2026
 */

@WebMvcTest(LaundryRoomController.class)
class LaundryRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ILaundryRoomService service;

    private LaundryRoom buildRoom(int roomId, String roomNumber, int capacity, boolean active) {
        return new LaundryRoom.Builder()
                .roomId(roomId)
                .roomNumber(roomNumber)
                .location("Residence A")
                .capacity(capacity)
                .description("Ground Floor")
                .isActive(active)
                .build();
    }

    @Test
    void createRoom_withValidParams_returns200() throws Exception {
        when(service.createRoom("R001", "Residence A", 10, "Ground Floor"))
                .thenReturn(buildRoom(1, "R001", 10, false));

        mockMvc.perform(post("/laundry-room/create")
                        .param("roomNumber", "R001")
                        .param("location", "Residence A")
                        .param("capacity", "10")
                        .param("description", "Ground Floor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("R001"));
    }

    @Test
    void getAllRooms_returns200() throws Exception {
        when(service.getAllRooms()).thenReturn(List.of(buildRoom(1, "R001", 10, false)));

        mockMvc.perform(get("/laundry-room/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getActiveRooms_returns200() throws Exception {
        when(service.getActiveRooms()).thenReturn(List.of(buildRoom(1, "R001", 10, true)));

        mockMvc.perform(get("/laundry-room/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getRoomById_withExistingRoom_returns200() throws Exception {
        when(service.getRoomById(1)).thenReturn(Optional.of(buildRoom(1, "R001", 10, false)));

        mockMvc.perform(get("/laundry-room/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("R001"));
    }

    @Test
    void getRoomById_withNonExistentRoom_returns404() throws Exception {
        when(service.getRoomById(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/laundry-room/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRoom_withValidParams_returns200() throws Exception {
        when(service.updateRoom(eq(1), eq("Residence B"), eq(20), eq("Updated Room")))
                .thenReturn(buildRoom(1, "R001", 20, false));

        mockMvc.perform(put("/laundry-room/1/update")
                        .param("location", "Residence B")
                        .param("capacity", "20")
                        .param("description", "Updated Room"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.capacity").value(20));
    }

    @Test
    void updateRoom_withNonExistentRoom_returns404() throws Exception {
        when(service.updateRoom(eq(999), eq("Loc"), eq(4), eq("desc")))
                .thenThrow(new NoSuchElementException("LaundryRoom not found with id: 999"));

        mockMvc.perform(put("/laundry-room/999/update")
                        .param("location", "Loc")
                        .param("capacity", "4")
                        .param("description", "desc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateRoom_withExistingInactiveRoom_returns200() throws Exception {
        when(service.activateRoom(1)).thenReturn(buildRoom(1, "R001", 10, true));

        mockMvc.perform(put("/laundry-room/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void activateRoom_withAlreadyActiveRoom_returns409() throws Exception {
        when(service.activateRoom(1)).thenThrow(new IllegalStateException("Room is already active."));

        mockMvc.perform(put("/laundry-room/1/activate"))
                .andExpect(status().isConflict());
    }

    @Test
    void deactivateRoom_withExistingActiveRoom_returns200() throws Exception {
        when(service.deactivateRoom(1)).thenReturn(buildRoom(1, "R001", 10, false));

        mockMvc.perform(put("/laundry-room/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void deleteRoom_withExistingRoom_returns204() throws Exception {
        when(service.deleteRoom(1)).thenReturn(true);

        mockMvc.perform(delete("/laundry-room/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRoom_withNonExistentRoom_returns404() throws Exception {
        when(service.deleteRoom(999)).thenReturn(false);

        mockMvc.perform(delete("/laundry-room/999"))
                .andExpect(status().isNotFound());
    }
}