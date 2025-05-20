package com.ian.tablereservation.store.application;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.store.dto.StoreDto;
import com.ian.tablereservation.store.domain.Store;
import com.ian.tablereservation.store.domain.StoreRepository;
import com.ian.tablereservation.store.table.dto.StoreTableDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final KakaoGeocodingApiService kakaoGeocodingApiService;


    /**
     * 정렬 기준과 주소를 바탕으로 가게 목록을 조회합니다.
     *
     * @param sort    정렬 기준 (alphabet (default), distance, rating)
     * @param address 거리 정렬 시 기준이 되는 주소 (선택)
     * @return 정렬된 가게 응답 리스트
     * @throws RuntimeException 가게가 없거나 주소 정보가 잘못된 경우
     */
    public List<StoreDto.StoreResponse> getSortStores(String sort, String address) {
        log.info("가게 목록 조회 요청 처리 시작");
        log.debug("요청 정렬 기준: {}", sort);

        List<Store> stores = switch (sort) {
            case "rating" -> {
                log.debug("정렬 방식: 별점순");

                yield storeRepository.findAllByOrderByRatingDesc()
                        .orElseThrow(() -> {
                            log.error("별점순 정렬 실패 - 등록된 가게 없음");
                            return new RuntimeException("등록된 가게가 없습니다.");
                        });
            }

            case "distance" -> {
                log.debug("정렬 방식: 거리순");

                if (!StringUtils.hasText(address)) {
                    log.error("거리순 정렬 실패 - 주소값 없음");
                    throw new IllegalArgumentException("위치 정보를 불러올 수 없습니다.");
                }

                KakaoGeocodingApiService.LatLng coordinates =
                        kakaoGeocodingApiService.getCoordinates(address);
                Double lat = coordinates.getLat();
                Double lng = coordinates.getLng();
                log.debug("주소 변환 좌표: lat={}, lng={}", lat, lng);

                List<Store> storeList = storeRepository.findAll();

                if (storeList.isEmpty()) {
                    log.error("거리순 정렬 실패 - 등록된 가게 없음");
                    throw new RuntimeException("등록된 가게가 없습니다.");
                }

                storeList.sort(Comparator.comparingDouble(
                        store -> distance(lat, lng, store.getLatitude(), store.getLongitude())
                ));

                yield storeList;
            }

            default -> {
                log.debug("정렬 방식: 가나다순");
                yield storeRepository.findAllByOrderByNameAsc()
                        .orElseThrow(() -> {
                            log.error("가나다순 정렬 실패 - 등록된 가게 없음");
                            return new RuntimeException("등록된 가게가 없습니다.");
                        });
            }
        };

        log.info("가게 목록 조회 요청 처리 성공");
        return stores.stream().map(StoreDto.StoreResponse::from).toList();
    }

    /**
     * 두 좌표 간 유클리디안 거리 계산
     */
    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }


    /**
     * 가게 이름에 해당하는 키워드로 검색합니다.
     *
     * @param keyword 검색 키워드
     * @return 검색된 가게 응답 리스트
     * @throws RuntimeException 검색 결과 없음
     */
    public List<StoreDto.StoreResponse> searchStore(String keyword) {
        log.info("가게 검색 요청 처리 시작: 키워드={}", keyword);
        var stores = storeRepository.findByNameContainingIgnoreCase(keyword)
                .orElseThrow(() -> {
                    log.error("가게 검색 실패 - 검색 결과 없음");
                    return new RuntimeException("검색 결과와 일치하는 가게가 없습니다.");
                });

        log.info("가게 검색 요청 처리 성공: 검색 결과 수={}", stores.size());
        return stores.stream().map(StoreDto.StoreResponse::from).toList();
    }


    /**
     * 새로운 가게 정보를 등록합니다.
     *
     * @param request 가게 등록 요청 정보
     * @param user    등록 요청자
     * @return 등록된 가게 정보 응답
     */
    @Transactional
    public StoreDto.StoreInfoResponse createStore(
            StoreDto.CreateStoreRequest request, CustomUserDetails user
    ) {
        log.info("가게 등록 요청 수신: 이름={}", request.getName());

        Long storeId = generateStoreId();
        log.debug("생성된 가게 고유 번호: {}", storeId);

        KakaoGeocodingApiService.LatLng coordinates =
                kakaoGeocodingApiService.getCoordinates(request.getAddress());
        log.debug("주소 → 좌표 변환 결과: lat={}, lng={}", coordinates.getLat(), coordinates.getLng());

        Store store = storeRepository.save(
                Store.builder()
                        .storeId(storeId)
                        .name(request.getName())
                        .description(request.getDescription())
                        .address(request.getAddress())
                        .latitude(coordinates.getLat())
                        .longitude(coordinates.getLng())
                        .user(user.getUser())
                        .tables(new ArrayList<>())
                        .build()
        );

        store.updateTables(request.getTables());

        log.info("가게 등록 완료: 이름={}, ID={}", store.getName(), store.getStoreId());
        return StoreDto.StoreInfoResponse.from(store);
    }

    /**
     * UUID 기반으로 가게 고유 번호를 생성합니다.
     */
    private Long generateStoreId() {
        return Math.abs(UUID.randomUUID().getMostSignificantBits());
    }


    /**
     * 고유 번호로 가게 정보를 조회합니다.
     *
     * @param storeId 가게 고유 번호
     * @return 가게 응답 정보
     * @throws RuntimeException 가게가 존재하지 않는 경우
     */
    public StoreDto.StoreInfoResponse getStore(Long storeId) {
        log.info("가게 단일 조회 요청: ID={}", storeId);
        Store store = findStoreOrThrow(storeId);

        log.info("가게 조회 성공: 이름={}", store.getName());
        return StoreDto.StoreInfoResponse.from(store);
    }


    /**
     * 가게 정보를 수정합니다.
     *
     * @param storeId 가게 고유 번호
     * @param request 수정 요청 정보
     * @param user    수정 요청자
     * @return 수정된 가게 응답
     */
    @Transactional
    public StoreDto.StoreInfoResponse updateStore(
            Long storeId, StoreDto.UpdateStoreRequest request, CustomUserDetails user
    ) {
        log.info("가게 수정 요청 수신: ID={}", storeId);
        Store store = findStoreOrThrow(storeId);
        validateStoreOwner(user, store);

        if (StringUtils.hasText(request.getName())) {
            log.debug("가게 이름 변경 요청 → {}", request.getName());
            store.updateName(request.getName());
        }

        if (StringUtils.hasText(request.getAddress())) {
            log.debug("가게 주소 변경 요청 → {}", request.getAddress());
            KakaoGeocodingApiService.LatLng coordinates =
                    kakaoGeocodingApiService.getCoordinates(request.getAddress());

            store.updateAddress(request.getAddress(), coordinates.getLat(), coordinates.getLng());
        }

        if (StringUtils.hasText(request.getDescription())) {
            log.debug("가게 소개 변경 → {}", request.getDescription());
            store.updateDescription(request.getDescription());
        }

        if (request.getTables() != null && !request.getTables().isEmpty()) {
            log.debug("가게 테이블 정보 변경");
            store.updateTables(request.getTables());
        }

        log.info("가게 수정 요청 처리 성공");
        return StoreDto.StoreInfoResponse.from(store);
    }


    /**
     * 가게 정보를 삭제합니다.
     *
     * @param storeId 삭제할 가게 고유 번호
     * @param user    삭제 요청자
     * @throws RuntimeException 권한 불일치 또는 존재하지 않는 가게
     */
    @Transactional
    public void deleteStore(Long storeId, CustomUserDetails user) {
        log.info("가게 삭제 요청 처리: 가게 고유 번호={}", storeId);
        Store store = findStoreOrThrow(storeId);

        validateStoreOwner(user, store);

        log.info("가게 삭제 요청 처리 성공");
        storeRepository.deleteByStoreId(storeId);
    }


    /**
     * 고유 번호로 가게를 조회하거나 없을 경우 예외 발생
     *
     * @param storeId 가게 고유 번호
     * @return 조회된 가게
     * @throws RuntimeException 존재하지 않을 경우
     */
    private Store findStoreOrThrow(Long storeId) {
        return storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new RuntimeException("가게 정보를 찾을 수 없습니다."));
    }


    /**
     * 현재 사용자가 가게 소유자인지 검증합니다.
     *
     * @param user  현재 사용자
     * @param store 가게 정보
     * @throws RuntimeException 소유주 불일치 시 예외 발생
     */
    private static void validateStoreOwner(CustomUserDetails user, Store store) {
        if (!store.getUser().getPhone().equals(user.getUsername())) {
            log.error("해당 작업을 수행할 권한 없음: 소유주={}, 요청자={}", store.getUser().getPhone(), user.getUsername());
            throw new RuntimeException("가게 소유주 정보가 일치하지 않습니다.");
        }
    }
}