package com.ian.tablereservation.entity;

import com.ian.tablereservation.dto.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "store")
public class StoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 테이블 아이디

    private String name; // 가게 이름
    private String location; // 가게 위치
    private String description; // 가게 설명

    public Store.Response toDto() {
        return Store.Response.builder()
                .storeId(id)
                .name(name)
                .location(location)
                .description(description)
                .build();
    }
}
