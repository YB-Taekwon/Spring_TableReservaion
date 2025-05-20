package com.ian.tablereservation.reservation.dto;

import com.ian.tablereservation.store.table.domain.StoreTable;

import java.time.LocalDateTime;

public record ValidatedReservation(
        StoreTable table,
        Integer numberOfPeople,
        LocalDateTime start,
        LocalDateTime end
) {
}