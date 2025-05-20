package com.ian.tablereservation.reservation.ui;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.reservation.application.UserReservationService;
import com.ian.tablereservation.reservation.dto.ReservationDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class UserReservationController {

    private final UserReservationService reservationService;


    /**
     * 새로운 예약 정보를 생성합니다.
     *
     * @param request 예약 요청 DTO
     * @param user    인증된 사용자
     * @return 생성된 예약 정보
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationDto.CreateReservationRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 생성 요청 수신: 사용자={}", user.getUsername());

        ReservationDto.ReservationResponse reservationResponse =
                reservationService.createReservation(request, user);

        log.info("예약 생성 완료: 예약 ID={}", reservationResponse.getReservationId());
        return ResponseEntity.ok(reservationResponse);
    }


    /**
     * 예약 상세 정보를 조회합니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @return 예약 상세 정보
     */
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> getReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 상세 조회 요청: 예약 ID={}", reservationId);

        ReservationDto.ReservationResponse response =
                reservationService.getReservation(reservationId, user);

        log.info("예약 상세 조회 완료: 예약 ID={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }


    /**
     * 예약 정보를 수정합니다.
     *
     * @param reservationId 예약 고유 ID
     * @param request       예약 수정 요청 DTO
     * @param user          인증된 사용자
     * @return 수정된 예약 정보
     */
    @PutMapping("/{reservationId}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReservationDto.UpdateReservationRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 수정 요청: 예약 ID={}", reservationId);

        ReservationDto.ReservationResponse response =
                reservationService.updateReservation(reservationId, request, user);

        log.info("예약 수정 완료: 예약 ID={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }


    /**
     * 예약 정보를 삭제합니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<?> deleteReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("예약 삭제 요청: 예약 ID={}", reservationId);

        reservationService.deleteReservation(reservationId, user);

        log.info("예약 삭제 완료: 예약 ID={}", reservationId);
        return ResponseEntity.ok("예약 삭제가 완료되었습니다.");
    }


    /**
     * 예약의 체크인을 처리합니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @return 체크인 완료된 예약 정보
     */
    @PostMapping("/{reservationId}/checkin")
    public ResponseEntity<?> checkin(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("체크인 요청: 예약 ID={}", reservationId);

        ReservationDto.ReservationResponse response = reservationService.checkin(reservationId, user);

        log.info("체크인 완료: 예약 ID={}", response.getReservationId());
        return ResponseEntity.ok(response);
    }
}