package com.ian.tablereservation.controller;

import com.ian.tablereservation.Service.StoreService;
import com.ian.tablereservation.dto.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;


    /**
     * 가게 목록 API
     *
     * @return
     */
    @GetMapping
    public ResponseEntity<?> getAllStores() {
        var stores = storeService.getAllStores();

        return ResponseEntity.ok(stores);
    }


    /**
     * 가게 상세 조회 API
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
     *
     * @param request: 가게 이름, 가게 위치, 가게 설명
     * @return 가게 등록 성공 시 가게 정보(번호, 이름, 위치, 설명) 반환
     */
    @PostMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addStore(@RequestBody Store.Request request) {
        log.info("가게 등록: {}", request.getName());
        var response = storeService.addStore(request);

        return ResponseEntity.ok(response);
    }


    /**
     * 가게 수정 API
     *
     * @param storeId: 가게 번호
     * @param request: 가게 정보(이름, 위치, 설명)
     * @return 가게 수정 성공 시 수정된 가게 정보 반환
     */
    @PatchMapping("/{storeId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStore(
            @PathVariable Long storeId, @RequestBody Store.Request request) {
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
    public ResponseEntity<?> deleteStore(@PathVariable Long storeId) {
        log.info("가게 삭제: id={}", storeId);
        storeService.deleteStore(storeId);

        return ResponseEntity.noContent().build();
    }
}
