package com.ian.tablereservation.store.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<List<Store>> findAllByOrderByNameAsc();

    Optional<List<Store>> findAllByOrderByRatingDesc();

    Optional<List<Store>> findByNameContainingIgnoreCase(String keyword);

    Optional<Store> findByStoreId(Long storeId);

    void deleteByStoreId(Long storeId);
}
