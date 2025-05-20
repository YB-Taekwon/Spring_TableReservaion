package com.ian.tablereservation.reservation.ui;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.reservation.application.PartnerReservationService;
import com.ian.tablereservation.reservation.dto.ReservationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/stores/{storeId}/reservations/{reservationId}")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PARTNER')")
public class PartnerReservationController {

    private final PartnerReservationService reservationService;


    /**
     * 예약을 승인합니다.
     * 요청자는 PARTNER 권한을 보유하고 있어야 합니다.
     *
     * @param storeId       가게 고유 ID
     * @param reservationId 승인할 예약 ID
     * @param user          인증된 파트너 사용자 정보
     * @return 승인된 예약 응답
     */
    @PostMapping("/approve")
    public ResponseEntity<?> approveReservation(
            @PathVariable Long storeId,
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 승인 요청 수신: reservationId={}, manager={}", reservationId, user.getUsername());

        ReservationDto.ReservationResponse response =
                reservationService.approveReservation(storeId, reservationId, user);

        log.info("예약 승인 완료: reservationId={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }


    /**
     * 예약을 거절합니다.
     *
     * @param storeId       가게 고유 ID
     * @param reservationId 거절할 예약 ID
     * @param user          인증된 파트너 사용자 정보
     * @return 거절된 예약 응답
     */
    @PostMapping("reject")
    public ResponseEntity<?> rejectReservation(
            @PathVariable Long storeId,
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 거절 요청 수신: reservationId={}, manager={}", reservationId, user.getUsername());
        ReservationDto.ReservationResponse response =
                reservationService.rejectReservation(storeId, reservationId, user);

        log.info("예약 거절 완료: reservationId={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }
}
