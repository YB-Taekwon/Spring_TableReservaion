package com.ian.tablereservation.entity;

import com.ian.tablereservation.dto.Reservation;
import com.ian.tablereservation.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "reservation")
public class ReservationEntity extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user; // 사용자 정보

    @ManyToOne
    @JoinColumn(name = "store_id")
    private StoreEntity store; // 가게 정보

    @ManyToOne
    @JoinColumn(name = "table_id")
    private TableEntity table; // 테이블 정보

    @Column(nullable = false)
    private LocalDateTime reservationDateTime; // 예약 날짜 및 시간

    @Column(nullable = false)
    private Integer numberOfPeople; // 예약 인원

    @Enumerated(EnumType.STRING)
    private ReservationStatus status; // 예약 상태
}
