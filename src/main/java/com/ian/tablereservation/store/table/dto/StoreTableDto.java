package com.ian.tablereservation.store.table.dto;

import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.table.domain.StoreTable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreTableDto {

    @NotNull
    @Size(min = 1)
    private Integer number;

    @NotNull
    @Min(1)
    private Integer capacity;

    public static StoreTableDto from(StoreTable table) {
        return StoreTableDto.builder()
                .number(table.getNumber())
                .capacity(table.getCapacity())
                .build();
    }
}