package com.ian.tablereservation.Service;

import com.ian.tablereservation.dto.Store;
import com.ian.tablereservation.entity.StoreEntity;
import com.ian.tablereservation.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;


    // 가게 목록 메서드
    public List<Store.Response> getAllStores() {
        return storeRepository.findAll().stream()
                .map(StoreEntity::toDto)
                .toList();
    }


    // 가게 상세 조회 메서드
    public Store.Response getStore(Long storeId) {
        StoreEntity storeEntity = findStore(storeId);

        return storeEntity.toDto();
    }


    // 가게 등록 메서드
    @Transactional
    public Store.Response addStore(Store.Request store) {
        StoreEntity storeEntity = storeRepository.save(store.toEntity());

        return storeEntity.toDto();
    }


    // 가게 수정 메서드
    @Transactional
    public Store.Response updateStore(Long storeId, Store.Request store) {
        StoreEntity storeEntity = findStore(storeId);

        if (store.getName() != null) storeEntity.setName(store.getName());
        if (store.getLocation() != null) storeEntity.setLocation(store.getLocation());
        if (store.getDescription() != null) storeEntity.setDescription(store.getDescription());

        return storeEntity.toDto();
    }

    private StoreEntity findStore(Long storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("일치하는 가게 정보를 찾을 수 없습니다."));
    }


    // 가게 삭제 메서드
    @Transactional
    public void deleteStore(Long storeId) {
        // 가게 등록 여부 확인
        if (!storeRepository.existsById(storeId))
            throw new RuntimeException("일치하는 가게 정보를 찾을 수 없습니다.");

        storeRepository.deleteById(storeId);
    }

}
