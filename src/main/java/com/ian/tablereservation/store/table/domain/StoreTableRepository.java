package com.ian.tablereservation.store.table.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreTableRepository extends JpaRepository<StoreTable, Long> {
}