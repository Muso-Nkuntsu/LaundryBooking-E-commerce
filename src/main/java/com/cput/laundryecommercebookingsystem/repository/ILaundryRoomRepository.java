package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */
public interface ILaundryRoomRepository extends JpaRepository<LaundryRoom, Integer>{


    List<LaundryRoom> findByIsActive(boolean isActive);

    List<LaundryRoom> findByLocation(String location);

    Optional<LaundryRoom> findByRoomNumber(String roomNumber);
}
