package com.example.devopsproj.service;

import com.example.devopsproj.analyzer.LeakAnalyzer;
import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.model.HeapSnapshot;
import com.example.devopsproj.model.LeakReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeakAnalysisService {

    private final LeakAnalyzer leakAnalyzer;
    private final SimulationService simulationService;
    private final GarbageCollectionService garbageCollectionService;

    @Transactional
    public LeakReport generateLeakReport() {
        log.info("Service: Generating leak report");

        simulationService.createHeapSnapshot();

        return leakAnalyzer.analyzeAndGenerateReport();
    }

    public List<HeapSnapshot> getHeapHistory(LocalDateTime startTime, LocalDateTime endTime) {
        return simulationService.getHeapHistory(startTime, endTime);
    }

    public List<GCEvent> getGCHistory() {
        return garbageCollectionService.getGCHistory();
    }

    public List<LeakReport> getAllReports() {
        return leakAnalyzer.getAllReports();
    }

    public Optional<LeakReport> getLatestReport() {
        return leakAnalyzer.getLatestReport();
    }
}
