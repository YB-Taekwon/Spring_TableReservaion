package com.ian.tablereservation.dto;

import com.ian.tablereservation.entity.StoreEntity;
import lombok.*;

public class Store {
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String name;
        private String location;
        private String description;

        public StoreEntity toEntity() {
            return StoreEntity.builder()
                    .name(name)
                    .location(location)
                    .description(description)
                    .build();
        }
    }

    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long storeId;
        private String name;
        private String location;
        private String description;
    }


}
