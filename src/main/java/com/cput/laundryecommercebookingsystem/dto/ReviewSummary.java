package com.cput.laundryecommercebookingsystem.dto;

import java.util.Map;

/**
 * Totals shown at the top of the reviews page.
 * ratingDistribution maps each star value (1 to 5) to how many reviews gave it.
 */
public record ReviewSummary(double averageRating, long totalReviews, Map<Integer, Long> ratingDistribution) {
}
