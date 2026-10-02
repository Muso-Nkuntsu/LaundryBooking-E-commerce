package com.cput.laundryecommercebookingsystem.dto;

/** Body of POST /review/create. */
public record CreateReviewRequest(Long studentId, Long serviceId, int rating, String comment) {
}
