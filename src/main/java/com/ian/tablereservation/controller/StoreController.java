package com.ian.tablereservation.controller;

import com.ian.tablereservation.service.StoreService;
import com.ian.tablereservation.dto.Store;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;


    /**
     * 가게 목록 API
     * GET /stores
     *
     * @param sort alphabet: 가나다순 -> default
     *             rating: 별점순
     *             distance: 거리순
     * @param lat: 거리순 정렬 시 필요한 y좌표
     * @param lng: 거리순 정렬 시 필요한 x좌표
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getSortStores(
            @RequestParam(defaultValue = "alphabet") String sort,
            @RequestParam(required = false, name = "lat") Double lat,
            @RequestParam(required = false, name = "lng") Double lng
    ) {
        log.info("가게 목록: 정렬 기준={}, 좌표={}, {}", sort, lat, lng);
        var stores = storeService.getSortStores(sort, lat, lng);

        return ResponseEntity.ok(stores);
    }


    /**
     * 가게 검색 API
     * GET /stores/search?keyword=
     *
     * @param keyword: 검색어
     * @return 검색어가 들어간 모든 가게 리스트 반환
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchStore(@RequestParam String keyword) {
        log.info(keyword);
        var stores = storeService.searchStore(keyword);

        return ResponseEntity.ok(stores);
    }


    /**
     * 가게 상세 조회 API
     * GET /stores/{storeId}
     *
     * @param storeId
     * @return
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStore(@PathVariable Long storeId) {
        var store = storeService.getStore(storeId);

        return ResponseEntity.ok(store);
    }


    /**
     * 가게 등록 API
     * POST /stores
     *
     * @param request: 가게 이름, 가게 위치, 가게 설명
     * @return 가게 등록 성공 시 가게 정보(번호, 이름, 위치, 설명) 반환
     */
    @PostMapping
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> addStore(
            @RequestBody @Valid Store.AddRequest request,
            Authentication authentication
    ) {
        log.info("가게 등록={}", request.getName());
        var store = storeService.addStore(request);

        return ResponseEntity.ok(store);
    }


    /**
     * 가게 수정 API
     *
     * @param storeId: 가게 번호
     * @param request: 가게 정보(이름, 위치, 설명)
     * @return 가게 수정 성공 시 수정된 가게 정보 반환
     */
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateStore(
            @PathVariable Long storeId,
            @RequestBody Store.UpdateRequest request,
            Authentication authentication
    ) {
        log.info("가게 수정: id={}, name={}", storeId, request.getName());
        var response = storeService.updateStore(storeId, request);

        return ResponseEntity.ok(response);
    }


    /**
     * 가게 삭제 API
     *
     * @param storeId
     * @return
     */
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(
            @PathVariable Long storeId,
            Authentication authentication
    ) {
        log.info("가게 삭제: id={}", storeId);
        storeService.deleteStore(storeId);

        return ResponseEntity.noContent().build();
    }
}
