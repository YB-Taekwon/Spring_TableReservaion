package com.ian.tablereservation.dto;

import com.ian.tablereservation.entity.StoreEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class Store {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddRequest {
        @NotBlank
        private String name; // 가게 이름
        @NotBlank
        private String address; // 가게 주소
        @NotNull
        private String description; // 가게 설명
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name; // 가게 이름
        private String address; // 가게 주소
        private String description; // 가게 설명
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long storeId; // 가게 등록 번호
        private String name; // 가게 이름
        private String address; // 가게 주소
        private Double latitude; // 위도 (가게 주소)
        private Double longitude; // 경도 (가게 주소)
        private String description; // 가게 설명
        private Double rating; // 가게 별점
    }

    public static Store.Response toDto(StoreEntity storeEntity) {
        return Store.Response.builder()
                .storeId(storeEntity.getId())
                .name(storeEntity.getName())
                .address(storeEntity.getAddress())
                .latitude(storeEntity.getLatitude())
                .longitude(storeEntity.getLongitude())
                .description(storeEntity.getDescription())
                .build();
    }
}
