package com.example.devopsproj.repository;

import com.example.devopsproj.model.LeakReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeakReportRepository extends JpaRepository<LeakReport, Long> {

    @Query("SELECT l FROM LeakReport l ORDER BY l.createdAt DESC")
    List<LeakReport> findAllOrderByCreatedAtDesc();

    @Query("SELECT l FROM LeakReport l ORDER BY l.createdAt DESC LIMIT 1")
    Optional<LeakReport> findLatestReport();
}
