package com.ian.tablereservation.review.application;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.reservation.domain.Reservation;
import com.ian.tablereservation.reservation.domain.ReservationRepository;
import com.ian.tablereservation.review.domain.Review;
import com.ian.tablereservation.review.domain.ReviewRepository;
import com.ian.tablereservation.review.dto.ReviewDto;
import com.ian.tablereservation.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

import static com.ian.tablereservation.common.enums.Role.ROLE_PARTNER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;


    /**
     * 리뷰를 생성하고 해당 가게의 평균 평점을 업데이트합니다.
     * 가게에 실제로 방문한 예약자만 리뷰를 작성할 수 있습니다.
     *
     * @param reservationId 예약 ID
     * @param request       리뷰 생성 요청
     * @param user          현재 로그인한 사용자
     * @return 생성된 리뷰 응답 DTO
     */
    @Transactional
    public ReviewDto.ReviewResponse createReview(
            Long reservationId, ReviewDto.CreateReviewRequest request, CustomUserDetails user
    ) {
        log.info("리뷰 생성 요청: reservationId={}, user={}", reservationId, user.getUsername());

        Reservation reservation = findReservationOrThrow(reservationId);
        validateReviewAuthor(reservation.getUser(), user);

        if (reservation.getStatus().isNotCompleted()) {
            log.error("가게 미방문: reservationStatus={}", reservation.getStatus());
            throw new IllegalStateException("가게에 실제로 방문한 사용자만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByReservation(reservation)) {
            log.error("리뷰 이미 존재");
            throw new RuntimeException("리뷰를 이미 작성하셨습니다.");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .content(request.getContent())
                .image(Optional.ofNullable(request.getImage()).orElse(""))
                .user(user.getUser())
                .reservation(reservation)
                .build();

        review.linkReservationAndStore(reservation);

        Review result = reviewRepository.save(review);

        updateStoreRating(reservation);

        log.info("리뷰 생성 완료: reviewId={}, reservationId={}", review.getId(), reservationId);
        return ReviewDto.ReviewResponse.from(result);
    }


    /**
     * 예약 및 리뷰 ID로 리뷰를 조회합니다.
     *
     * @param reservationId 예약 ID
     * @param reviewId      리뷰 ID
     * @return 리뷰 응답 DTO
     */
    public ReviewDto.ReviewResponse getReview(Long reservationId, Long reviewId) {
        log.debug("리뷰 단건 조회 시도: reservationId={}, reviewId={}", reservationId, reviewId);
        Review review = findReviewOrThrow(reservationId, reviewId);

        return ReviewDto.ReviewResponse.from(review);
    }


    /**
     * 리뷰를 수정하고 필요한 경우 가게의 평점을 갱신합니다.
     * 리뷰의 작성자만 수정할 수 있습니다.
     *
     * @param reservationId 예약 ID
     * @param reviewId      리뷰 ID
     * @param request       수정 요청
     * @param user          현재 로그인한 사용자
     * @return 수정된 리뷰 응답 DTO
     */
    @Transactional
    public ReviewDto.ReviewResponse updateReview(
            Long reservationId,
            Long reviewId,
            ReviewDto.UpdateReviewRequest request,
            CustomUserDetails user
    ) {
        log.info("리뷰 수정 요청: reservationId={}, reviewId={}, user={}", reservationId, reviewId, user.getUsername());

        Review review = findReviewOrThrow(reservationId, reviewId);
        validateReviewAuthor(review.getUser(), user);

        Reservation reservation = findReservationOrThrow(reservationId);

        boolean ratingUpdated = false;

        if (request.getRating() != null) {
            log.debug("리뷰 별점 수정: {}", request.getRating());
            review.updateRating(request.getRating());
            ratingUpdated = true;
        }

        if (StringUtils.hasText(request.getContent())) {
            log.debug("리뷰 내용 수정: {}", request.getContent());
            review.updateContent(request.getContent());
        }

        if (StringUtils.hasText(request.getImage())) {
            log.debug("리뷰 사진 수정: {}", request.getImage());
            review.updateImage(request.getImage());
        }

        if (ratingUpdated) {
            review.linkReservationAndStore(reservation);
            updateStoreRating(reservation);
            log.debug("가게 평점 갱신 완료: storeId={}", review.getReservation().getStore().getId());
        }

        log.info("리뷰 수정 완료: reviewId={}", review.getId());
        return ReviewDto.ReviewResponse.from(review);
    }


    /**
     * 리뷰를 삭제하고 해당 가게의 평점을 갱신합니다.
     *
     * @param reservationId 예약 ID
     * @param reviewId      리뷰 ID
     * @param user          현재 로그인한 사용자
     */
    @Transactional
    public void deleteReview(Long reservationId, Long reviewId, CustomUserDetails user) {
        log.info("리뷰 삭제 요청: reservationId={}, reviewId={}, user={}", reservationId, reviewId, user.getUsername());
        Review review = findReviewOrThrow(reservationId, reviewId);

        boolean isPartner = user.getUser().getRole().equals(ROLE_PARTNER);
        boolean isWriter = review.getUser().getPhone().equals(user.getUsername());

        if (!isPartner && !isWriter) {
            log.error("리뷰 삭제 권한 없음: user={}, role={}", user.getUsername(), user.getUser().getRole());
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }

        reviewRepository.delete(review);
        updateStoreRating(review.getReservation());

        log.info("리뷰 삭제 완료: reviewId={}", reviewId);
    }


    /**
     * 예약 ID를 기반으로 예약 정보를 조회합니다.
     * 예약 정보가 존재하지 않는 경우 예외를 발생시킵니다.
     *
     * @param reservationId 예약 고유 ID
     * @return 예약 객체
     * @throws EntityNotFoundException 예약 정보가 존재하지 않는 경우
     */
    private Reservation findReservationOrThrow(Long reservationId) {
        log.debug("예약 조회 시도: ID={}", reservationId);

        return reservationRepository.findByReservationId(reservationId)
                .orElseThrow(() -> {
                    log.error("예약 조회 실패 - 존재하지 않음: ID={}", reservationId);
                    return new EntityNotFoundException("예약 정보를 찾을 수 없습니다.");
                });
    }


    /**
     * 예약 ID 및 리뷰 ID를 기반으로 리뷰 정보를 조회합니다.
     * 리뷰 정보가 존재하지 않으면 예외를 발생시킵니다.
     *
     * @param reservationId 예약 ID
     * @param reviewId      리뷰 ID
     * @return 리뷰 객체
     * @throws EntityNotFoundException 리뷰 정보가 존재하지 않는 경우
     */
    private Review findReviewOrThrow(Long reservationId, Long reviewId) {
        log.debug("리뷰 조회 시도: ID={}", reviewId);

        return reviewRepository.findByIdAndReservation_ReservationId(reviewId, reservationId)
                .orElseThrow(() -> {
                    log.error("리뷰가 존재하지 않음: reservationId={}, reviewId={}", reservationId, reviewId);
                    return new EntityNotFoundException("리뷰를 찾을 수 없습니다.");
                });
    }


    /**
     * 리뷰 또는 예약의 작성자가 현재 로그인한 사용자와 일치하는지 확인합니다.
     *
     * @param targetUser  리뷰 또는 예약 작성자
     * @param currentUser 현재 로그인한 사용자
     */
    private static void validateReviewAuthor(User targetUser, CustomUserDetails currentUser) {
        if (!targetUser.getPhone().equals(currentUser.getUsername())) {
            log.error("리뷰 권한 없음: 작성자={}, 현재 사용자={}", targetUser.getPhone(), currentUser.getUsername());
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }
    }


    /**
     * 해당 예약의 가게 평점을 리뷰 평균으로 재계산하여 업데이트합니다.
     */
    private void updateStoreRating(Reservation reservation) {
        Long storeId = reservation.getStore().getId();
        Double rating = reviewRepository.calculateAvgRatingByStore(storeId);
        reservation.getStore().updateRating(rating);
        log.debug("가게 평점 업데이트: storeId={}, newRating={}", storeId, rating);
    }
}