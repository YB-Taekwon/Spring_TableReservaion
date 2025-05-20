package com.ian.tablereservation.reservation.application;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.reservation.domain.Reservation;
import com.ian.tablereservation.reservation.domain.ReservationRepository;
import com.ian.tablereservation.reservation.dto.ReservationDto;
import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.domain.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import static com.ian.tablereservation.reservation.domain.ReservationStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartnerReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;


    /**
     * 예약을 승인합니다.
     * 이미 처리된 예약이거나 작업을 수행할 권한이 없는 경우 예외를 발생시킵니다.
     *
     * @param reservationId 예약 ID
     * @param user          인증된 파트너 사용자 정보
     * @return 승인된 예약 정보
     * @throws IllegalArgumentException 이미 처리된 예약인 경우
     * @throws AccessDeniedException    권한이 없는 경우
     */
    @Transactional
    public ReservationDto.ReservationResponse approveReservation(
            Long storeId, Long reservationId, CustomUserDetails user
    ) {
        log.info("예약 승인 처리 시작: reservationId={}, manager={}", reservationId, user.getUsername());

        Reservation reservation = findReservationOrThrow(reservationId, storeId);
        log.debug("예약 조회 성공: store={}, status={}", reservation.getStore().getName(), reservation.getStatus());

        validateStoreManagerAccess(storeId, user);
        isNotPending(reservation);

        reservation.updateStatus(CONFIRMED);

        log.info("예약 승인 처리 완료: reservationId={}", reservationId);
        return ReservationDto.ReservationResponse.from(reservation);
    }


    @Transactional
    public ReservationDto.ReservationResponse rejectReservation(
            Long storeId, Long reservationId, CustomUserDetails user
    ) {
        log.info("예약 거절 처리 시작: reservationId={}, manager={}", reservationId, user.getUsername());

        Reservation reservation = findReservationOrThrow(reservationId, storeId);
        log.debug("예약 조회 성공: store={}, status={}", reservation.getStore().getName(), reservation.getStatus());

        validateStoreManagerAccess(storeId, user);
        isNotPending(reservation);

        reservation.updateStatus(CANCELLED);

        log.info("예약 거절 처리 완료: reservationId={}", reservationId);
        return ReservationDto.ReservationResponse.from(reservation);
    }


    /**
     * 예약 ID를 기반으로 예약 정보를 조회합니다.
     * 예약 정보가 존재하지 않는 경우 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @return 예약 객체
     * @throws RuntimeException 예약 정보가 존재하지 않는 경우
     */
    private Reservation findReservationOrThrow(Long reservationId, Long storeId) {
        log.debug("예약 조회 시도: ID={}", reservationId);

        return reservationRepository.findByReservationIdAndStore_StoreId(reservationId, storeId)
                .orElseThrow(() -> {
                    log.error("예약 조회 실패 - 존재하지 않음: ID={}", reservationId);
                    return new RuntimeException("예약 정보를 찾을 수 없습니다.");
                });
    }


    /**
     *
     * @param storeId
     * @param user
     */
    private void validateStoreManagerAccess(Long storeId, CustomUserDetails user) {
        log.debug("점장 권한 검증 시도: {}", user.getUsername());

        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> {
                    log.error("");
                    return new EntityNotFoundException("가게 정보를 찾을 수 없습니다.");
                });

        String ownerPhone = store.getUser().getPhone();
        String requestUser = user.getUsername();

        if (!ownerPhone.equals(requestUser)) {
            log.error("예약 접근 거부 - 점장 권한 없음: 점장={}, 요청자={}", ownerPhone, requestUser);
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }

        log.debug("점장 권한 검증 성공: 점장={}, 요청자={}", ownerPhone, requestUser);
    }


    /**
     * 예약 상태가 처리 전인지 검증합니다.
     *
     * @param reservation 예약 객체
     * @throws IllegalArgumentException 이미 승인/거절된 예약인 경우
     */
    private static void isNotPending(Reservation reservation) {
        if (reservation.getStatus().isNotPending()) {
            log.error("예약 처리 실패 - 이미 처리된 상태: reservationId={}, status={}",
                    reservation.getReservationId(), reservation.getStatus());
            throw new IllegalArgumentException("이미 처리된 예약입니다.");
        }

        log.debug("예약 상태 확인 완료 - 처리 전 상태");
    }
}
