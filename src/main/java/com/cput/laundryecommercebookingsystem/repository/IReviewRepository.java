package com.cput.laundryecommercebookingsystem.repository;

import com.cput.laundryecommercebookingsystem.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */
public interface IReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByStudentStudentId(Long studentId);

    List<Review> findByLaundryServiceServiceId(Long serviceId);

    List<Review> findByRating(int rating);
}
