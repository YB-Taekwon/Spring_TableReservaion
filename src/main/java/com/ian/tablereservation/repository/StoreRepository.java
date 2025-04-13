package com.ian.tablereservation.repository;

import com.ian.tablereservation.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    boolean existsById(Long id);
}
