package com.example.devopsproj.repository;

import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.model.GCType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GCEventRepository extends JpaRepository<GCEvent, Long> {

    List<GCEvent> findByGcType(GCType gcType);

    List<GCEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT g FROM GCEvent g ORDER BY g.timestamp DESC")
    List<GCEvent> findAllOrderByTimestampDesc();

    @Query("SELECT AVG(g.reclaimedMemory) FROM GCEvent g")
    Double calculateAverageReclaimedMemory();

    @Query("SELECT AVG(CAST(g.reclaimedMemory AS double) / g.beforeHeap) FROM GCEvent g WHERE g.beforeHeap > 0")
    Double calculateAverageGCEfficiency();
}
