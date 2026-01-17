package com.example.devopsproj.repository;

import com.example.devopsproj.model.HeapSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HeapSnapshotRepository extends JpaRepository<HeapSnapshot, Long> {

    List<HeapSnapshot> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h FROM HeapSnapshot h ORDER BY h.timestamp DESC")
    List<HeapSnapshot> findAllOrderByTimestampDesc();

    @Query("SELECT h FROM HeapSnapshot h ORDER BY h.timestamp DESC LIMIT 1")
    Optional<HeapSnapshot> findLatestSnapshot();

    @Query("SELECT h FROM HeapSnapshot h ORDER BY h.timestamp ASC LIMIT 1")
    Optional<HeapSnapshot> findFirstSnapshot();
}
