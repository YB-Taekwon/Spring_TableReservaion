package com.ian.tablereservation.controller;

import com.ian.tablereservation.service.ReservationService;
import com.ian.tablereservation.dto.Reservation;
import com.ian.tablereservation.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;


    /**
     * 이용자 - 예약 요청 API
     *
     * @param createRequest: 가게 정보, 예약자 전화번호, 예약 인원 예약 날짜, 예약 시간
     * @param user:          사용자 로그인 상태
     * @return 예약 번호, 가게 정보, 예약자 정보(전화번호), 예약 인원, 예약 날짜 및 시간, 예약 상태 반환
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createReservation(
            @RequestBody Reservation.CreateRequest createRequest,
            @AuthenticationPrincipal UserEntity user
    ) {
        Reservation.Response response = reservationService.createReservation(createRequest, user);

        return ResponseEntity.ok(response);
    }


    /**
     * 점장 - 예약 승인 및 거절 API
     *
     * @param reservationId: 예약 번호
     * @param updateRequest: 예약 상태 정보
     * @param partner:       점장 로그인 상태
     * @return
     */
    @PatchMapping("/{reservationId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> confirmReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody Reservation.UpdateRequest updateRequest,
            @AuthenticationPrincipal UserEntity partner
    ) {
        Reservation.Response response = reservationService.updateReservationStatus(reservationId, updateRequest, partner);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/{reservationId}/checkin")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> checkin(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal UserEntity user
    ) {
        Reservation.Response response = reservationService.checkin(reservationId, user);

        return ResponseEntity.ok(response);
    }
}
