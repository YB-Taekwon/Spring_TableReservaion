package com.ian.tablereservation.reservation.dto;

import com.ian.tablereservation.reservation.domain.Reservation;
import com.ian.tablereservation.reservation.domain.ReservationStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationDto {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationRequest {

        @NotNull
        private Long tableId;

        @NotNull
        @Min(1)
        private Integer numberOfPeople;

        @NotNull
        private LocalDate date;

        @NotNull
        private LocalTime time;

        public LocalDateTime toDateTime() {
            return LocalDateTime.of(date, time);
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReservationResponse {
        private Long reservationId;
        private Long storeId;
        private Long tableId;
        private String phone;
        private Integer numberOfPeople;
        private LocalDate date;
        private LocalTime time;
        private ReservationStatus status;

        public static ReservationResponse from(Reservation reservation) {
            return ReservationResponse.builder()
                    .reservationId(reservation.getReservationId())
                    .storeId(reservation.getStore().getStoreId())
                    .tableId(reservation.getTable().getId())
                    .phone(reservation.getUser().getPhone())
                    .numberOfPeople(reservation.getNumberOfPeople())
                    .date(reservation.getStartDateTime().toLocalDate())
                    .time(reservation.getStartDateTime().toLocalTime())
                    .status(reservation.getStatus())
                    .build();
        }
    }
}
