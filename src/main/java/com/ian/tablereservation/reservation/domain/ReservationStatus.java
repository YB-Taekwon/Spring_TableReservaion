package com.ian.tablereservation.reservation.domain;

import java.time.LocalDateTime;

public enum ReservationStatus {
    REQUESTED,
    CONFIRMED,
    CANCELLED,
    COMPLETED;

    public boolean canCheckin() {
        return this == CONFIRMED;
    }

    public boolean isNoShow(LocalDateTime now, LocalDateTime start) {
        return this == CONFIRMED && now.isAfter(start.plusMinutes(10));
    }

    public boolean isNotPending() {
        return this != REQUESTED;
    }

    public boolean isNotCompleted() {
        return this != COMPLETED;
    }
}
