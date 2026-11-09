package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.LaundryRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 *  * Talent Nocuze
 *  * 230405886
 *  * 25 July 2026
 *  */

@Repository
public interface ILaundryRoomRepository extends JpaRepository<LaundryRoom, Long>{


    List<LaundryRoom> findByIsActive(boolean isActive);

    List<LaundryRoom> findByLocation(String location);

    Optional<LaundryRoom> findByRoomNumber(String roomNumber);
}
