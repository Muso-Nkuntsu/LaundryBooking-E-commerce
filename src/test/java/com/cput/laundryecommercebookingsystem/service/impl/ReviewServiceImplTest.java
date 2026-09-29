package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import com.cput.laundryecommercebookingsystem.domain.Review;
import com.cput.laundryecommercebookingsystem.domain.Student;
import com.cput.laundryecommercebookingsystem.repository.ILaundryServiceRepository;
import com.cput.laundryecommercebookingsystem.repository.IReviewRepository;
import com.cput.laundryecommercebookingsystem.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ReviewServiceImplTest.java
 *
 * Lindokuhle Nanto
 * 240443608
 * 28 July 2026
 */
public class ReviewServiceImplTest {

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ILaundryServiceRepository laundryServiceRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review review;
    private Student student;
    private LaundryService laundryService;
    private LocalDateTime date;

    @BeforeEach
    void setUp() {

        student = mock(Student.class);
        laundryService = mock(LaundryService.class);
        date = LocalDateTime.now();

        review = Review.builder()
                .student(student)
                .laundryService(laundryService)
                .rating(5)
                .comment("Excellent laundry service!")
                .date(date)
                .build();
    }

    @Test
    void createReview_returnsSavedReview() {

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        when(laundryServiceRepository.findById(1L))
                .thenReturn(Optional.of(laundryService));

        when(reviewRepository.save(any(Review.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        Review result =
                reviewService.createReview(
                        1L,
                        1L,
                        5,
                        "Excellent laundry service!",
                        date
                );

        assertNotNull(result);
        assertEquals(
                5,
                result.getRating()
        );
        assertEquals(
                "Excellent laundry service!",
                result.getComment()
        );
        assertEquals(
                student,
                result.getStudent()
        );
        assertEquals(
                laundryService,
                result.getLaundryService()
        );

        verify(studentRepository)
                .findById(1L);

        verify(laundryServiceRepository)
                .findById(1L);

        verify(reviewRepository)
                .save(any(Review.class));
    }

    @Test
    void getReviewById_returnsReview() {

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        Optional<Review> result =
                reviewService.getReviewById(1L);

        assertTrue(result.isPresent());
        assertEquals(
                5,
                result.get().getRating()
        );

        verify(reviewRepository)
                .findById(1L);
    }

    @Test
    void getReviewsByStudent_returnsList() {

        when(reviewRepository
                .findByStudentStudentId(1L))
                .thenReturn(List.of(review));

        List<Review> result =
                reviewService.getReviewsByStudent(1L);

        assertEquals(1, result.size());
        assertEquals(
                5,
                result.get(0).getRating()
        );

        verify(reviewRepository)
                .findByStudentStudentId(1L);
    }

    @Test
    void getReviewsByLaundryService_returnsList() {

        when(reviewRepository
                .findByLaundryServiceServiceId(1L))
                .thenReturn(List.of(review));

        List<Review> result =
                reviewService
                        .getReviewsByLaundryService(1L);

        assertEquals(1, result.size());
        assertEquals(
                "Excellent laundry service!",
                result.get(0).getComment()
        );

        verify(reviewRepository)
                .findByLaundryServiceServiceId(1L);
    }

    @Test
    void getAllReviews_returnsList() {

        when(reviewRepository.findAll())
                .thenReturn(List.of(review));

        List<Review> result =
                reviewService.getAllReviews();

        assertEquals(1, result.size());

        verify(reviewRepository)
                .findAll();
    }

    @Test
    void deleteReview_callsRepository() {

        when(reviewRepository.findById(1L))
                .thenReturn(Optional.of(review));

        reviewService.deleteReview(1L);

        verify(reviewRepository)
                .delete(review);
    }
}

