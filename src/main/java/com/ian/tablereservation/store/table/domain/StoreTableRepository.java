package com.ian.tablereservation.store.table.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {
    Optional<StoreTable> findByIdAndStore_StoreId(Long tableId, Long storeId);
}