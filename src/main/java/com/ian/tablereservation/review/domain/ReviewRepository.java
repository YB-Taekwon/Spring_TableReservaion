package com.ian.tablereservation.review.domain;

import com.ian.tablereservation.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByReservation(Reservation reservation);

    Optional<Review> findByIdAndReservation_ReservationId(Long reviewId, Long reservationId);

    @Query("select avg(r.rating) from Review r where r.reservation.store.id = :storeId")
    Double calculateAvgRatingByStore(@Param("storeId") Long storeId);
}