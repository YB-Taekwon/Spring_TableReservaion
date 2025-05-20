package com.ian.tablereservation.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByReservationIdAndStore_StoreId(Long reservationId, Long storeId);

    Optional<Reservation> findByReservationId(Long reservationId);

    @Query("""
            select count(r) > 0
            from Reservation r
            where r.table.id = :tableId
            and r.status = 'CONFIRMED'
            and not (
            :end <= r.startDateTime or :start >= r.endDateTime
            )
            """)
    boolean isTableReserved(Long tableId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByStatusAndStartDateTimeBefore(ReservationStatus status, LocalDateTime thresholdTime);
}