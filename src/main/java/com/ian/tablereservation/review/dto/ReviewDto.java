package com.ian.tablereservation.review.dto;

import com.ian.tablereservation.review.domain.Review;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ReviewDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateReviewRequest {

        @NotNull
        private Integer rating;

        @NotEmpty
        private String content;

        private String image;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateReviewRequest {

        private Integer rating;
        private String content;
        private String image;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewResponse {
        private Long reviewId;
        private Integer rating;
        private String content;
        private String image;
        private String writer;

        public static ReviewResponse from(Review review) {
            return ReviewResponse.builder()
                    .reviewId(review.getId())
                    .rating(review.getRating())
                    .content(review.getContent())
                    .image(review.getImage())
                    .writer(review.getUser().getPhone().substring(7))
                    .build();
        }
    }
}