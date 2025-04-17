package com.ian.tablereservation.repository;

import com.ian.tablereservation.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    // 가게 목록 조회 - 가나다순 정렬
    Optional<List<StoreEntity>> findAllByOrderByNameAsc();

    // 가게 목록 조회 - 별점순 정렬
    Optional<List<StoreEntity>> findAllByOrderByRatingDesc();

    // 가게 검색
    Optional<List<StoreEntity>> findByNameContainingIgnoreCase(String keyword);

    // 가게 존재 여부 확인
    boolean existsById(Long id);
}
