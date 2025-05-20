package com.ian.tablereservation.reservation.application;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.reservation.dto.ReservationDto;
import com.ian.tablereservation.reservation.domain.Reservation;
import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.table.domain.StoreTable;
import com.ian.tablereservation.reservation.domain.ReservationRepository;
import com.ian.tablereservation.store.domain.StoreRepository;
import com.ian.tablereservation.store.table.domain.StoreTableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.ian.tablereservation.reservation.domain.ReservationStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final StoreTableRepository tableRepository;

    private static final int ALLOWED_TIME_UNIT_MINUTES = 30;


    /**
     * 사용자의 예약 생성 요청을 처리합니다.
     * 요청된 테이블과 시간대의 유효성을 검증하고, 예약 정보를 저장한 뒤 응답 DTO를 반환합니다.
     *
     * @param request 예약 생성 요청 정보
     * @param user    인증된 사용자 정보
     * @return 생성된 예약에 대한 응답 DTO
     * @throws IllegalArgumentException 유효하지 않은 시간 또는 인원일 경우
     * @throws IllegalStateException    테이블이 이미 예약되어 있거나 예약 인원 초과인 경우
     */
    @Transactional
    public ReservationDto.ReservationResponse createReservation(
            ReservationDto.CreateReservationRequest request, CustomUserDetails user
    ) {
        log.info("예약 생성 처리 시작: 사용자={}", user.getUsername());
        Store store = findStoreOrThrow(request.getStoreId());

        Long tableId = request.getTableId();
        LocalDateTime start = request.toDateTime();
        LocalDateTime end = start.plusMinutes(ALLOWED_TIME_UNIT_MINUTES);

        log.debug("예약 요청 시간: 시작={}, 종료={}", start, end);

        validateTableAvailability(tableId, start, end, request.getNumberOfPeople());

        StoreTable table = findTableOrThrow(tableId);

        Long reservationId = generateReservationId();
        log.debug("예약 번호 생성: 예약 번호={}", reservationId);

        Reservation reservation = reservationRepository.save(
                Reservation.builder()
                        .reservationId(reservationId)
                        .store(store)
                        .user(user.getUser())
                        .numberOfPeople(request.getNumberOfPeople())
                        .startDateTime(start)
                        .endDateTime(end)
                        .table(table)
                        .status(REQUESTED)
                        .build()
        );

        log.info("예약 생성 완료: ID={}", reservationId);
        return ReservationDto.ReservationResponse.from(reservation);
    }

    /**
     * UUID를 기반으로 예약 번호를 생성합니다.
     */
    private Long generateReservationId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }


    /**
     * 특정 예약 정보를 상세 조회합니다.
     * 예약이 존재하지 않거나 예약자와 사용자 정보가 일치하지 않으면 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @return 예약 상세 정보
     * @throws RuntimeException 예약이 존재하지 않거나 사용자 불일치 시
     */
    public ReservationDto.ReservationResponse getReservation(Long reservationId, CustomUserDetails user) {
        log.info("예약 상세 조회 처리: 예약 ID={}", reservationId);

        Reservation reservation = findReservationOrThrow(reservationId);
        validateReservationOwner(reservation, user);

        log.info("예약 조회 성공: 사용자={}, 예약 상태={}", user.getUsername(), reservation.getStatus());
        return ReservationDto.ReservationResponse.from(reservation);
    }


    /**
     * 예약 정보를 수정합니다.
     *
     * @param reservationId 예약 고유 ID
     * @param request       예약 수정 요청 DTO
     * @param user          인증된 사용자
     * @return 수정된 예약 정보
     */
    @Transactional
    public ReservationDto.ReservationResponse updateReservation(
            Long reservationId,
            ReservationDto.UpdateReservationRequest request,
            CustomUserDetails user
    ) {
        log.info("예약 수정 처리 시작: 예약 ID={}", reservationId);

        Reservation reservation = findReservationOrThrow(reservationId);
        validateReservationOwner(reservation, user);

        Long tableId = request.getTableId();
        LocalDateTime start = request.toDateTime();
        LocalDateTime end = start.plusMinutes(ALLOWED_TIME_UNIT_MINUTES);

        log.debug("예약 수정 요청 내용: 테이블={}, 시작={}, 종료={}, 인원={}",
                tableId, start, end, request.getNumberOfPeople());

        validateTableAvailability(tableId, start, end, request.getNumberOfPeople());

        StoreTable table = findTableOrThrow(tableId);

        reservation.updateReservation(table, request.getNumberOfPeople(), start, end);

        log.info("예약 수정 완료: 예약 ID={}", reservationId);
        return ReservationDto.ReservationResponse.from(reservation);
    }


    /**
     * 예약 정보를 삭제합니다.
     * 예약이 존재하지 않거나 예약자와 사용자가 일치하지 않는 경우 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @throws RuntimeException 예약이 존재하지 않거나 사용자 불일치 시
     */
    public void deleteReservation(Long reservationId, CustomUserDetails user) {
        log.info("예약 삭제 요청 처리 시작: 예약 ID={}, 사용자={}", reservationId, user.getUsername());
        Reservation reservation = findReservationOrThrow(reservationId);

        validateReservationOwner(reservation, user);

        log.info("예약 삭제 완료: 예약 ID={}", reservationId);
        reservationRepository.delete(reservation);
    }


    /**
     * 예약에 대한 체크인 처리를 수행합니다.
     * 예약 승인 상태가 아니거나 입장 시간이 아닌 경우 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @param user          인증된 사용자
     * @return 체크인 완료된 예약 정보
     * @throws IllegalStateException 상태가 승인되지 않았거나, 입장 가능 시간이 아닐 경우
     */
    public ReservationDto.ReservationResponse checkin(Long reservationId, CustomUserDetails user) {
        log.info("체크인 요청 처리 시작: 예약 ID={}, 사용자={}", reservationId, user.getUsername());

        Reservation reservation = findReservationOrThrow(reservationId);
        validateReservationOwner(reservation, user);

        if (reservation.getStatus().canCheckin()) {
            log.error("체크인 불가 - 현재 상태: {}", reservation.getStatus());
            throw new IllegalStateException("예약이 승인된 상태만 도착 확인이 가능합니다.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = reservation.getStartDateTime();

        if (now.isBefore(start.minusMinutes(10)) || now.isAfter(start.plusMinutes(10))) {
            log.error("체크인 실패 - 입장 가능 시간 아님");
            throw new IllegalStateException("도착 확인 가능 시간이 아닙니다.");
        }

        reservation.updateStatus(COMPLETED);

        log.info("체크인 완료: 예약 ID={}", reservationId);
        return ReservationDto.ReservationResponse.from(reservation);
    }


    /**
     * 스케쥴러를 통해 5분 간격으로 예약의 노쇼를 처리합니다.
     */
    @Scheduled(fixedRate = 300000)
    public void handleNoShows() {
        LocalDateTime now = LocalDateTime.now();

        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndStartDateTimeBefore(CONFIRMED, now.minusMinutes(10));

        for (Reservation reservation : expiredReservations) {
            if (reservation.getStatus().isNoShow(now, reservation.getStartDateTime())) {
                log.info("노쇼 처리됨: {}", reservation.getReservationId());
                reservation.updateStatus(CANCELLED);
            }
        }
    }


    /**
     * 지정된 테이블이 요청한 시간대에 예약 가능한지 검증합니다.
     * - 지나간 시간 예약 불가
     * - 30분 단위 시간만 예약 가능
     * - 동일한 시간 대 중복 예약 불가
     * - 예약 인원이 테이블 수용 인원 이하
     *
     * @param tableId        테이블 ID
     * @param start          예약 시작 시간
     * @param end            예약 종료 시간
     * @param numberOfPeople 예약 인원
     * @throws IllegalArgumentException 유효하지 않은 예약 조건
     * @throws IllegalStateException    중복 예약 시
     */
    private void validateTableAvailability(
            Long tableId, LocalDateTime start, LocalDateTime end, Integer numberOfPeople
    ) {
        log.debug("테이블 예약 가능 여부 확인: 테이블 ID={}, 인원={}, 시작={}, 종료={}",
                tableId, numberOfPeople, start, end);

        if (start.isBefore(LocalDateTime.now())) {
            log.error("예약 실패 - 과거 시간 요청: {}", start);
            throw new IllegalArgumentException("지나간 시간에는 예약할 수 없습니다.");
        }

        if (start.getMinute() % ALLOWED_TIME_UNIT_MINUTES != 0) {
            log.error("예약 실패 - 30분 단위가 아닌 시간 요청: {}", start);
            throw new IllegalArgumentException("예약 시간은 30분 단위로만 가능합니다.");
        }

        boolean isReserved = reservationRepository.isTableReserved(tableId, start, end);

        if (isReserved) {
            log.error("예약 실패 - 이미 예약된 시간: {}", start);
            throw new IllegalStateException("해당 시간에는 예약이 불가능합니다.");
        }

        StoreTable table = findTableOrThrow(tableId);
        if (numberOfPeople > table.getCapacity()) {
            log.error("예약 실패 - 테이블 수용 인원 초과: 요청={}, 최대={}", numberOfPeople, table.getCapacity());
            throw new IllegalArgumentException("예약 인원이 테이블 수용 인원을 초과했습니다.");
        }

        log.debug("테이블 예약 가능");
    }


    /**
     * 예약 ID로 예약 정보를 조회합니다.
     * 예약 정보가 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @return 예약 객체
     * @throws RuntimeException 예약 정보가 존재하지 않는 경우
     */
    private Reservation findReservationOrThrow(Long reservationId) {
        log.debug("예약 조회: ID={}", reservationId);

        return reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 조회 실패 - 존재하지 않음: ID={}", reservationId);
                    return new RuntimeException("예약 정보를 찾을 수 없습니다.");
                });
    }


    /**
     * 가게 ID로 가게 정보를 조회합니다.
     * 가게 정보가 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param storeId 가게 고유 ID
     * @return 가게 엔티티
     * @throws RuntimeException 가게 정보가 존재하지 않는 경우
     */
    private Store findStoreOrThrow(Long storeId) {
        log.debug("가게 조회: ID={}", storeId);

        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> {
                    log.error("가게 조회 실패 - 존재하지 않음: ID={}", storeId);
                    return new RuntimeException("가게 정보를 찾을 수 없습니다.");
                });
    }


    /**
     * 테이블 ID로 테이블 정보를 조회합니다.
     * 테이블 정보가 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param tableId 테이블 고유 ID
     * @return 테이블 엔티티
     * @throws RuntimeException 테이블 정보가 존재하지 않는 경우
     */
    private StoreTable findTableOrThrow(Long tableId) {
        log.debug("테이블 조회: ID={}", tableId);

        return tableRepository.findById(tableId)
                .orElseThrow(() -> {
                    log.error("테이블 조회 실패 - 존재하지 않음: ID={}", tableId);
                    return new RuntimeException("테이블을 찾을 수 없습니다.");
                });
    }


    /**
     * 예약자가 현재 로그인한 사용자와 일치하는지 검증합니다.
     *
     * @param reservation 예약 객체
     * @param user        현재 로그인한 사용자
     * @throws RuntimeException 사용자 불일치 시 예외 발생
     */
    private static void validateReservationOwner(Reservation reservation, CustomUserDetails user) {
        String ownerPhone = reservation.getUser().getPhone();
        String requestUser = user.getUsername();

        if (!ownerPhone.equals(requestUser)) {
            log.error("예약자 불일치: 예약자={}, 요청자={}", ownerPhone, requestUser);
            throw new RuntimeException("해당 작업을 수행할 권한이 없습니다.");
        }

        log.debug("예약자 확인 완료: {}", ownerPhone);
    }
}