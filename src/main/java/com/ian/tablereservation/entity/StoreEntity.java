package com.ian.tablereservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "store")
public class StoreEntity extends BaseEntity {
    @Column(nullable = false)
    private String name; // 가게 이름

    @Column(nullable = false)
    private String address; // 가게 주소

    @Column(nullable = false)
    private String description; // 가게 설명

    private Double latitude; // 위도 (가게 주소)
    private Double longitude; // 경도 (가게 주소)
    private Double rating; // 가게 별점

    @OneToMany(mappedBy = "store")
    private List<TableEntity> tables;
}
