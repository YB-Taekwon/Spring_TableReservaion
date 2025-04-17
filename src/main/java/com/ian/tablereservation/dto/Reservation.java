package com.ian.tablereservation.dto;

import com.ian.tablereservation.entity.ReservationEntity;
import com.ian.tablereservation.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Reservation {
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long storeId; // 가게 번호
        private String phone; // 예약자 전화번호
        private Integer numberOfPeople; // 예약 인원
        private LocalDate date; // 예약 날짜
        private LocalTime time; // 예약 시간

        public LocalDateTime getReservationDate() {
            return LocalDateTime.of(date, time);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private ReservationStatus status; // 예약 상태
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long reservationId; // 예약 번호
        private Long storeId; // 가게 번호
        private String phone; // 예약자 전화번호
        private Integer numberOfPeople; // 예약 인원
        private LocalDateTime reservationDateTime; // 예약 날짜 및 시간
        private ReservationStatus status; // 예약 상태

        public static Reservation.Response toDto(ReservationEntity reservation) {
            return Response.builder()
                    .reservationId(reservation.getId())
                    .storeId(reservation.getStore().getId())
                    .phone(reservation.getUser().getPhone())
                    .numberOfPeople(reservation.getNumberOfPeople())
                    .reservationDateTime(reservation.getReservationDateTime())
                    .status(reservation.getStatus())
                    .build();
        }
    }
}
