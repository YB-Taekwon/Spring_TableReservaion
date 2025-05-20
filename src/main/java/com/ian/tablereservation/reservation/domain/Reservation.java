package com.ian.tablereservation.reservation.domain;

import com.ian.tablereservation.common.base.BaseEntity;
import com.ian.tablereservation.reservation.dto.ValidatedReservation;
import com.ian.tablereservation.review.domain.Review;
import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.table.domain.StoreTable;
import com.ian.tablereservation.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.ian.tablereservation.reservation.domain.ReservationStatus.REQUESTED;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Column(unique = true, nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private Integer numberOfPeople;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne
    @JoinColumn(name = "table_id")
    private StoreTable table;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Review review;

    public void updateReservation(ValidatedReservation validatedReservation) {
        this.table = validatedReservation.table();
        this.numberOfPeople = validatedReservation.numberOfPeople();
        this.startDateTime = validatedReservation.start();
        this.endDateTime = validatedReservation.end();

        if (status.canCheckin()) status = REQUESTED;
    }

    public void updateStatus(ReservationStatus status) {
        this.status = status;
    }
}