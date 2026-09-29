package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Review;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.factory.ReviewFactory;
import com.cput.laundryecommercebookingsystem.repository.IReviewRepository;
import com.cput.laundryecommercebookingsystem.repository.ILaundryServiceRepository;
import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.service.IReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * ReviewServiceImpl.java
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */

@Service
public class ReviewServiceImpl implements IReviewService {

    private final IReviewRepository reviewRepository;
    private final IStudentRepository studentRepository;
    private final ILaundryServiceRepository laundryServiceRepository;

    public ReviewServiceImpl(
            IReviewRepository reviewRepository,
            IStudentRepository studentRepository,
            ILaundryServiceRepository laundryServiceRepository) {

        this.reviewRepository = reviewRepository;
        this.studentRepository = studentRepository;
        this.laundryServiceRepository = laundryServiceRepository;
    }

    @Override
    @Transactional
    public Review createReview(
            Long studentId,
            Long serviceId,
            int rating,
            String comment) {

        Student student =
                studentRepository.findById(studentId)
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "Student not found with id: "
                                                + studentId
                                ));

        LaundryService laundryService =
                laundryServiceRepository.findById(String.valueOf(serviceId))
                        .orElseThrow(() ->
                                new NoSuchElementException(
                                        "LaundryService not found with id: "
                                                + serviceId
                                ));

        Review review =
                ReviewFactory.create(
                        student,
                        laundryService,
                        rating,
                        comment
                );

        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> getReviewById(
            Long reviewId) {

        return reviewRepository.findById(reviewId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByStudent(
            Long studentId) {

        return reviewRepository.findByStudentStudentId(studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByLaundryService(
            Long serviceId) {

        return reviewRepository
                .findByLaundryServiceServiceId(serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {

        return reviewRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteReview(
            Long reviewId) {

        Review review =
                getReviewOrThrow(reviewId);

        reviewRepository.delete(review);
    }

    private Review getReviewOrThrow(
            Long reviewId) {

        return reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Review not found with id: "
                                        + reviewId
                        ));
    }

    public Review createReview(long L, long L1, int I, String S, LocalDateTime Date) {
        return null;
    }
}