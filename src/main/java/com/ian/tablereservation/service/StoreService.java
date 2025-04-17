package com.ian.tablereservation.service;

import com.ian.tablereservation.common.KakaoGeocodingApi;
import com.ian.tablereservation.dto.Store;
import com.ian.tablereservation.entity.StoreEntity;
import com.ian.tablereservation.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final KakaoGeocodingApi kakaoGeocodingApi;


    // 거리 계산 메서드 (유클리드)
    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }

    // 가게 목록 메서드 -> 정렬 필터 추가
    public List<Store.Response> getSortStores(String sort, Double lat, Double lng) {
        List<StoreEntity> stores = switch (sort) {
            // 별점순 정렬
            case "rating" -> storeRepository.findAllByOrderByRatingDesc()
                    .orElseThrow(() -> new RuntimeException("등록된 가게가 없습니다."));

            // 거리순 정렬
            case "distance" -> {
                if (lat == null || lng == null)
                    throw new IllegalArgumentException("위치 정보를 불러올 수 없습니다.");

                List<StoreEntity> list = storeRepository.findAll();

                if (list.isEmpty())
                    throw new RuntimeException("등록된 가게가 없습니다.");

                list.sort(Comparator.comparingDouble(
                        store -> distance(lat, lng, store.getLatitude(), store.getLongitude())
                ));
                yield list;
            }

            // 기본 정렬: 가나다순
            default -> storeRepository.findAllByOrderByNameAsc()
                    .orElseThrow(() -> new RuntimeException("등록된 가게가 없습니다."));
        };

        return stores.stream().map(Store::toDto).toList();
    }


    // 가게 검색 메서드
    public List<Store.Response> searchStore(String keyword) {
        var stores = storeRepository.findByNameContainingIgnoreCase(keyword)
                .orElseThrow(() -> new RuntimeException("검색 결과와 일치하는 가게가 없습니다."));

        return stores.stream().map(Store::toDto).toList();
    }


    // 가게 상세 조회 메서드
    public Store.Response getStore(Long storeId) {
        StoreEntity storeEntity = findStore(storeId);
        return Store.toDto(storeEntity);
    }


    // 가게 등록 메서드
    @Transactional
    public Store.Response addStore(Store.AddRequest request) {
        log.info(request.getAddress());
        KakaoGeocodingApi.LatLng coordinates = kakaoGeocodingApi.getCoordinates(request.getAddress());
        log.info("x={}, y={}", coordinates.getLng(), coordinates.getLat());

        StoreEntity store = StoreEntity.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(coordinates.getLat())
                .longitude(coordinates.getLng())
                .description(request.getDescription())
                .build();

        storeRepository.save(store);

        return Store.toDto(store);
    }


    // 가게 수정 메서드
    @Transactional
    public Store.Response updateStore(Long storeId, Store.UpdateRequest request) {
        StoreEntity store = findStore(storeId);

        if (request.getName() != null) store.setName(request.getName());
        if (request.getAddress() != null) store.setAddress(request.getAddress());
        if (request.getDescription() != null) store.setDescription(request.getDescription());

        return Store.toDto(store);
    }

    private StoreEntity findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("가게 정보를 찾을 수 없습니다."));
    }


    // 가게 삭제 메서드
    @Transactional
    public void deleteStore(Long storeId) {
        // 가게 등록 여부 확인
        if (!storeRepository.existsById(storeId))
            throw new RuntimeException("가게 정보를 찾을 수 없습니다.");

        storeRepository.deleteById(storeId);
    }
}
