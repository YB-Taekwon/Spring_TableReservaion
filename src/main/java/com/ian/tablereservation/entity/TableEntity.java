package com.ian.tablereservation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "store_table")
public class TableEntity extends BaseEntity {
    private Integer number; // 테이블 번호
    private Integer capacity; // 테이블 수용 인원

    @ManyToOne
    private StoreEntity store;
}
