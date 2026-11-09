package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.cput.laundryecommercebookingsystem.domain.LaundryMachine;
import com.cput.laundryecommercebookingsystem.domain.enums.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */
@Repository
public interface ILaundryMachineRepository extends JpaRepository<LaundryMachine, Long> {

    Optional<LaundryMachine> findByMachineNumber(String machineNumber);

    List<LaundryMachine> findByStatus(MachineStatus status);

    List<LaundryMachine> findByType(String type);

    List<LaundryMachine> findByLaundryRoom(LaundryRoom laundryRoom);

    /**
     * Loads the machine and locks its row until the transaction ends. Two students booking the same
     * machine at the same moment are then handled one after the other, so both can't get the slot.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM LaundryMachine m WHERE m.machineId = :id")
    Optional<LaundryMachine> findByIdForUpdate(@Param("id") Long id);
}
