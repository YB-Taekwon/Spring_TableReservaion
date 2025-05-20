package com.ian.tablereservation.store.domain;

import com.ian.tablereservation.common.base.BaseEntity;
import com.ian.tablereservation.store.table.domain.StoreTable;
import com.ian.tablereservation.store.table.dto.StoreTableDto;
import com.ian.tablereservation.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stores")
public class Store extends BaseEntity {

    @Column(nullable = false, unique = true)
    private Long storeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String description;

    private Double latitude;
    private Double longitude;
    private Double rating = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "store", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<StoreTable> tables = new ArrayList<>();

    public void updateName(String name) {
        this.name = name;
    }

    public void updateAddress(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateAddress(String address, Double latitude, Double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateTables(List<StoreTableDto> tables) {
        this.tables.clear();

        for (StoreTableDto dto : tables) {
            StoreTable table = StoreTable.builder()
                    .number(dto.getNumber())
                    .capacity(dto.getCapacity())
                    .build();
            table.assignStore(this);
            this.tables.add(table);
        }
    }

    public void updateRating(Double rating) {
        this.rating = rating;
    }
}