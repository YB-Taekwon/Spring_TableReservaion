package com.ian.tablereservation.repository;

import com.ian.tablereservation.entity.ReservationEntity;
import com.ian.tablereservation.entity.StoreEntity;
import com.ian.tablereservation.entity.TableEntity;
import com.ian.tablereservation.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    boolean existsByTableAndReservationDateTimeAndStatus(TableEntity table, LocalDateTime reservationDateTime, ReservationStatus status);
}
