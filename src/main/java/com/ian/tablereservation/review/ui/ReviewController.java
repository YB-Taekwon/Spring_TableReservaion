package com.ian.tablereservation.review.ui;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.review.application.ReviewService;
import com.ian.tablereservation.review.dto.ReviewDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/reservations/{reservationId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReview(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReviewDto.CreateReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ReviewDto.ReviewResponse response = reviewService.createReview(reservationId, request, user);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(@PathVariable Long reservationId, @PathVariable Long reviewId) {
        ReviewDto.ReviewResponse response = reviewService.getReview(reservationId, reviewId);

        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reservationId,
            @PathVariable Long reviewId,
            @RequestBody ReviewDto.UpdateReviewRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ReviewDto.ReviewResponse response = reviewService.updateReview(reservationId, reviewId, request, user);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'PARTNER')")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reservationId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        reviewService.deleteReview(reservationId, reviewId, user);

        return ResponseEntity.ok("리뷰를 성공적으로 삭제했습니다.");
    }
}