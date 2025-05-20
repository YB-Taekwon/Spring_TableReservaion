package com.ian.tablereservation.store.dto;

import com.ian.tablereservation.review.dto.ReviewDto;
import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.table.domain.StoreTable;
import com.ian.tablereservation.store.table.dto.StoreTableDto;
import com.ian.tablereservation.user.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StoreDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateStoreRequest {

        @NotBlank
        private String name;

        @NotBlank
        private String address;

        @NotBlank
        private String description;

        @NotEmpty
        private List<StoreTableDto> tables;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateStoreRequest {
        private String name;
        private String address;
        private String description;
        private List<StoreTableDto> tables;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreResponse {
        private Long storeId;
        private String name;
        private String address;
        private String description;
        private Double rating;

        public static StoreResponse from(Store store) {
            return StoreResponse.builder()
                    .storeId(store.getStoreId())
                    .name(store.getName())
                    .address(store.getAddress())
                    .description(store.getDescription())
                    .rating(store.getRating() != null ? store.getRating() : 0.0)
                    .build();
        }
    }


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreInfoResponse {
        private Long storeId;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private String description;
        private String owner;
        private List<StoreTableDto> tables;
        private Double rating;
        private List<ReviewDto.ReviewResponse> reviews;

        public static StoreInfoResponse from(Store store) {
            return StoreInfoResponse.builder()
                    .storeId(store.getStoreId())
                    .name(store.getName())
                    .address(store.getAddress())
                    .latitude(store.getLatitude())
                    .longitude(store.getLongitude())
                    .description(store.getDescription())
                    .owner(store.getUser().getName())
                    .tables(store.getTables().stream()
                            .map(StoreTableDto::from)
                            .toList()
                    )
                    .rating(store.getRating() != null ? store.getRating() : 0.0)
                    .reviews(store.getReviews().stream()
                            .map(ReviewDto.ReviewResponse::from)
                            .toList()
                    )
                    .build();
        }
    }
}