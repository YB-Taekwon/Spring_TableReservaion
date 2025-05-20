package com.ian.tablereservation.store.ui;

import com.ian.tablereservation.common.security.CustomUserDetails;
import com.ian.tablereservation.store.application.StoreService;
import com.ian.tablereservation.store.dto.StoreDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;


    /**
     * 정렬 기준과 주소 정보를 기반으로 가게 목록을 조회합니다.
     *
     * @param sort    정렬 기준 (가나다순(default), 거리순, 별점순)
     * @param address 거리 정렬 시 기준이 되는 주소 (선택)
     * @return 정렬된 가게 목록
     */
    @GetMapping
    public ResponseEntity<?> getSortStores(
            @RequestParam(defaultValue = "alphabet") String sort,
            @RequestParam(required = false) String address
    ) {
        log.info("가게 목록 조회 요청 수신");
        log.debug("정렬 기준: {}, 주소: {}", sort, address);

        List<StoreDto.StoreResponse> stores = storeService.getSortStores(sort, address);

        log.info("가게 목록 조회 성공: 총 {}건", stores.size());
        return ResponseEntity.ok(stores);
    }


    /**
     * 키워드를 기반으로 가게를 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 키워드에 해당하는 가게 목록
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchStore(@RequestParam String keyword) {
        log.info("가게 검색 요청 수신");
        log.debug("검색 키워드: {}", keyword);

        List<StoreDto.StoreResponse> stores = storeService.searchStore(keyword);

        log.info("가게 검색 완료: 검색 결과 {}건", stores.size());
        return ResponseEntity.ok(stores);
    }


    /**
     * 새로운 가게를 등록합니다.
     * 요청자는 반드시 PARTNER 권한을 가지고 있어야 합니다.
     *
     * @param request 가게 등록 요청 DTO
     * @param user    인증된 사용자 정보
     * @return 등록된 가게 정보
     */
    @PostMapping
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> createStore(
            @RequestBody @Valid StoreDto.CreateStoreRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("가게 등록 요청 수신");
        log.debug("요청자={}, 가게명={}", user.getUsername(), request.getName());

        StoreDto.StoreInfoResponse store = storeService.createStore(request, user);

        log.info("가게 등록 완료: ID={}, 이름={}", store.getStoreId(), store.getName());
        return ResponseEntity.ok(store);
    }


    /**
     * 특정 가게의 상세 정보를 조회합니다.
     *
     * @param storeId 조회할 가게의 고유 ID
     * @return 가게 상세 정보
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStore(@PathVariable Long storeId) {
        log.info("가게 상세 조회 요청 수신: ID={}", storeId);
        StoreDto.StoreInfoResponse store = storeService.getStore(storeId);

        log.info("가게 상세 조회 완료: 이름={}", store.getName());
        return ResponseEntity.ok(store);
    }


    /**
     * 가게 정보를 수정합니다.
     * 요청자는 반드시 PARTNER 권한을 가지고 있어야 합니다.
     *
     * @param storeId 가게 고유 ID
     * @param request 수정 요청 DTO
     * @param user    인증된 사용자 정보
     * @return 수정된 가게 정보
     */
    @PatchMapping("/{storeId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> updateStore(
            @PathVariable Long storeId,
            @RequestBody StoreDto.UpdateStoreRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("가게 수정 요청 수신: ID={}", storeId);
        log.debug("수정 내용: name={}, address={}, description={}",
                request.getName(), request.getAddress(), request.getDescription());

        StoreDto.StoreInfoResponse store = storeService.updateStore(storeId, request, user);

        log.info("가게 수정 완료: ID={}", storeId);
        return ResponseEntity.ok(store);
    }


    /**
     * 가게를 삭제합니다.
     * 요청자는 반드시 PARTNER 권한을 가지고 있어야 합니다.
     *
     * @param storeId 삭제할 가게의 고유 ID
     * @param user    인증된 사용자 정보
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/{storeId}")
    @PreAuthorize("hasRole('PARTNER')")
    public ResponseEntity<?> deleteStore(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("가게 삭제 요청 수신: ID={}, 요청자={}", storeId, user.getUsername());
        storeService.deleteStore(storeId, user);

        log.info("가게 삭제 완료: ID={}", storeId);
        return ResponseEntity.ok("가게 삭제에 성공했습니다.");
    }
}