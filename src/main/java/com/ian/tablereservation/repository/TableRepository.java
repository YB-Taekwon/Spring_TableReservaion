package com.ian.tablereservation.repository;

import com.ian.tablereservation.entity.StoreEntity;
import com.ian.tablereservation.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    List<TableEntity> findByStoreAndCapacityGreaterThanEqual(StoreEntity store, Integer capacity);
}
