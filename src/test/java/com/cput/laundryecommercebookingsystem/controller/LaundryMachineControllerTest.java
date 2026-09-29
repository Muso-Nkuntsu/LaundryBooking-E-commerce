package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import com.cput.laundryecommercebookingsystem.service.ILaundryMachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * LaundryMachineControllerTest.java
 *
 * Lindokuhle Nanto
 * 240443608
 * 29 July 2026
 */

@WebMvcTest(LaundryMachineController.class)
class LaundryMachineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILaundryMachineService laundryMachineService;

    private LaundryMachine laundryMachine;

    @BeforeEach
    void setUp() {
        laundryMachine = LaundryMachine.builder()
                .machineNumber("WM-101")
                .type("Washing Machine")
                .status(MachineStatus.AVAILABLE)
                .build();
    }

    @Test
    void createMachine_WhenValid_ShouldReturn201Created() throws Exception {
        when(laundryMachineService.createMachine("WM-101", "Washing Machine", MachineStatus.AVAILABLE, 1L))
                .thenReturn(laundryMachine);

        mockMvc.perform(post("/laundrymachine/create")
                        .param("machineNumber", "WM-101")
                        .param("type", "Washing Machine")
                        .param("status", "AVAILABLE")
                        .param("laundryRoomId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.machineNumber").value("WM-101"));
    }

    @Test
    void getMachineById_WhenFound_ShouldReturn200OK() throws Exception {
        when(laundryMachineService.getMachineById(1L)).thenReturn(Optional.of(laundryMachine));

        mockMvc.perform(get("/laundrymachine/read/{machineId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.machineNumber").value("WM-101"));
    }

    @Test
    void getMachineById_WhenNotFound_ShouldReturn404() throws Exception {
        when(laundryMachineService.getMachineById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/laundrymachine/read/{machineId}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateMachineStatus_WhenSuccess_ShouldReturn200OK() throws Exception {
        when(laundryMachineService.updateMachineStatus(eq(1L), eq(MachineStatus.IN_USE)))
                .thenReturn(laundryMachine);

        mockMvc.perform(patch("/laundrymachine/update-status/{machineId}", 1L)
                        .param("status", "IN_USE"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMachine_WhenExists_ShouldReturn204NoContent() throws Exception {
        doNothing().when(laundryMachineService).deleteMachine(1L);

        mockMvc.perform(delete("/laundrymachine/delete/{machineId}", 1L))
                .andExpect(status().isNoContent());

        verify(laundryMachineService, times(1)).deleteMachine(1L);
    }
}
