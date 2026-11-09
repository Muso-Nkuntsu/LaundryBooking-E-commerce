package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */
@Repository
public interface IReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStudentStudentId(Long studentId);

    // Was findByLaundryService(Long), which compared a LaundryService with a number and failed.
    List<Review> findByLaundryServiceId(Long serviceId);

    List<Review> findByRating(int rating);
}
