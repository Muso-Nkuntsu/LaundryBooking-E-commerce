package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Review;
import com.cput.laundryecommercebookingsystem.domain.Student;
import java.time.LocalDateTime;

/**
 * Lindokuhle Nanto
 * 240443608
 * 25 July 2026
 */

public class ReviewFactory {

    public static Review create(
            Student student,
            LaundryService laundryService,
            int rating,
            String comment,
            LocalDateTime date) {

        if (student == null || laundryService == null) {
            throw new IllegalArgumentException("Student and LaundryService are required");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        return Review.builder()
                .student(student)
                .laundryService(laundryService)
                .rating(rating)
                .comment(comment)
                .date(date)
                .build();
    }

    public static Review create(
            Student student,
            LaundryService laundryService,
            int rating,
            String comment) {

        return create(student, laundryService, rating, comment, LocalDateTime.now());
    }
}