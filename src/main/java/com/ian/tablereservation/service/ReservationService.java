package com.ian.tablereservation.service;

import com.ian.tablereservation.dto.Reservation;
import com.ian.tablereservation.entity.ReservationEntity;
import com.ian.tablereservation.entity.StoreEntity;
import com.ian.tablereservation.entity.TableEntity;
import com.ian.tablereservation.entity.UserEntity;
import com.ian.tablereservation.repository.ReservationRepository;
import com.ian.tablereservation.repository.StoreRepository;
import com.ian.tablereservation.repository.TableRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.ian.tablereservation.enums.ReservationStatus.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;
    private final TableRepository tableRepository;

    private static final int ALLOWED_TIME_UNIT_MINUTES = 30;


    // 이용자 - 예약 요청 메서드
    @Transactional
    public Reservation.Response createReservation(Reservation.CreateRequest createRequest, UserEntity user) {
        // 가게 조회
        StoreEntity store = storeRepository.findById(createRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException("가게 정보를 찾을 수 없습니다."));

        /* 예약 가능 여부 확인 */
        // LocalDate + LocalTime -> LocalDateTime 변환
        LocalDateTime reservationDateTime = createRequest.getReservationDate();
        // 예약 시간 유효성 검사
        validateReservationDateTime(reservationDateTime);

        // 예약 인원 이상 수용  가능한 테이블 중 예약 가능한 테이블 찾기
        TableEntity availableTable = findAvailableTable(
                store, createRequest.getNumberOfPeople(), reservationDateTime
        );
        // 예약 가능한 테이블이 없을 경우 예외 발생
        if (availableTable == null)
            throw new RuntimeException("예약 가능한 테이블이 없습니다.");

        // 예약이 가능한 테이블이 있을 경우, 해당 테이블로 예약
        ReservationEntity reservation = ReservationEntity.builder()
                .store(store)
                .user(user)
                .table(availableTable)
                .numberOfPeople(createRequest.getNumberOfPeople())
                .reservationDateTime(reservationDateTime)
                .status(REQUESTED)
                .build();

        reservationRepository.save(reservation);

        return Reservation.Response.toDto(reservation);
    }

    private TableEntity findAvailableTable(
            StoreEntity store, Integer numberOfPeople, LocalDateTime reservationDateTime
    ) {
        // 예약 인원 이상 수용 가능한 테이블 찾기
        List<TableEntity> candidateTables = tableRepository
                .findByStoreAndCapacityGreaterThanEqual(store, numberOfPeople);

        // 테이블 중 예약이 가능한 테이블 찾기
        for (TableEntity candidateTable : candidateTables) {
            boolean exists = reservationRepository.existsByTableAndReservationDateTimeAndStatus(
                    candidateTable, reservationDateTime, CONFIRMED
            );

            if (!exists) {
                return candidateTable;
            }
        }
        return null;
    }

    // 예약 시간 유효성 검사
    private static void validateReservationDateTime(LocalDateTime reservationDateTime) {
        // 과거 시간 예약 방지
        if (reservationDateTime.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("지나간 시간에는 예약할 수 없습니다.");

        // 예약 가능 시간 단위 - 30분
        int minute = reservationDateTime.getMinute();
        if (minute % ALLOWED_TIME_UNIT_MINUTES != 0)
            throw new IllegalArgumentException("예약 시간은 30분 단위로만 가능합니다.");
    }


    // 점장 - 예약 승인 및 거절 메서드
    @Transactional
    public Reservation.Response updateReservationStatus(Long reservationId, Reservation.UpdateRequest updateRequest, UserEntity partner) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 이미 이용이 완료된 경우 수정이 불가능
        if (reservation.getStatus() == COMPLETED)
            throw new IllegalArgumentException("이미 이용 완료된 요청입니다.");

        reservation.setStatus(updateRequest.getStatus());
        reservationRepository.save(reservation);

        return Reservation.Response.toDto(reservation);
    }


    // 키오스크 - 도착 확인 메서드 (로그인)
    public Reservation.Response checkin(Long reservationId, UserEntity user) {
        // 예약 정보 확인
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

        // 예약자 정보 확인
        if (!reservation.getUser().getId().equals(user.getId()))
            throw new RuntimeException("예약자 정보가 일치하지 않습니다.");

        // 예약 상태 확인
        if (reservation.getStatus() != CONFIRMED)
            throw new IllegalStateException("예약이 승인된 상태만 도착 확인이 가능합니다.");

        // 예약 시간 10분 전 ~ 10분 후만 체크인 가능
        LocalDateTime reservationDate = reservation.getReservationDateTime();

        boolean isCheckedIn = isWithinCheckinWindow(reservationDate);
        if (!isCheckedIn)
            throw new IllegalStateException("도착 확인 가능 시간이 아닙니다.");

        // 도착 확인
        reservation.setStatus(COMPLETED);
        reservationRepository.save(reservation);

        return Reservation.Response.toDto(reservation);
    }

    private static boolean isWithinCheckinWindow(LocalDateTime reservationDate) {
        LocalDateTime now = LocalDateTime.now();

        return !now.isBefore(reservationDate.minusMinutes(10)) &&
                !now.isAfter(reservationDate.plusMinutes(10));
    }
}
