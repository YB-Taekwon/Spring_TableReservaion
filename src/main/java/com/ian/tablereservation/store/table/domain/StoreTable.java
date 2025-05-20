package com.ian.tablereservation.store.table.domain;

import com.ian.tablereservation.common.base.BaseEntity;
import com.ian.tablereservation.store.domain.Store;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tables")
public class StoreTable extends BaseEntity {
    private Integer number;
    private Integer capacity;

    @ManyToOne
    private Store store;

    public void assignStore(Store store) {
        this.store = store;
    }
}