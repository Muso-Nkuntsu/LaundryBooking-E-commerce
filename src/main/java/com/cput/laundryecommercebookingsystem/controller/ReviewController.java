package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.domain.Review;
import com.cput.laundryecommercebookingsystem.dto.CreateReviewRequest;
import com.cput.laundryecommercebookingsystem.dto.ReviewSummary;
import com.cput.laundryecommercebookingsystem.service.IReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Student reviews of laundry services.
 */
@RestController
@RequestMapping("/review")
public class ReviewController {

    private final IReviewService reviewService;

    public ReviewController(IReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Example body:
     * { "studentId": 1, "serviceId": 2, "rating": 5, "comment": "Quick and clean." }
     */
    @PostMapping("/create")
    public ResponseEntity<Review> create(@RequestBody CreateReviewRequest request) {
        if (request.studentId() == null || request.serviceId() == null) {
            throw new IllegalArgumentException("studentId and serviceId are required");
        }
        Review created = reviewService.createReview(
                request.studentId(), request.serviceId(), request.rating(), request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Review> read(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** All reviews, newest first. */
    @GetMapping("/getall")
    public ResponseEntity<List<Review>> getAll() {
        return ResponseEntity.ok(newestFirst(reviewService.getAllReviews()));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<Review>> getByService(@PathVariable Long serviceId) {
        return ResponseEntity.ok(newestFirst(reviewService.getReviewsByLaundryService(serviceId)));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Review>> getByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(newestFirst(reviewService.getReviewsByStudent(studentId)));
    }

    /** Average rating, number of reviews, and how many reviews gave each star value. */
    @GetMapping("/summary")
    public ResponseEntity<ReviewSummary> getSummary() {
        List<Review> reviews = reviewService.getAllReviews();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            distribution.put(star, 0L);
        }
        for (Review review : reviews) {
            distribution.merge(review.getRating(), 1L, Long::sum);
        }

        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return ResponseEntity.ok(new ReviewSummary(average, reviews.size(), distribution));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    private List<Review> newestFirst(List<Review> reviews) {
        return reviews.stream()
                .sorted(Comparator.comparing(Review::getDate).reversed())
                .toList();
    }
}
