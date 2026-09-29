package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Review;

import java.util.List;
import java.util.Optional;

/**
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */

public interface IReviewService {

    Review createReview(
            Long studentId,
            Long serviceId,
            int rating,
            String comment
    );

    Optional<Review> getReviewById(
            Long reviewId
    );

    List<Review> getReviewsByStudent(
            Long studentId
    );

    List<Review> getReviewsByLaundryService(
            Long serviceId
    );

    List<Review> getAllReviews();

    void deleteReview(
            Long reviewId
    );
}

