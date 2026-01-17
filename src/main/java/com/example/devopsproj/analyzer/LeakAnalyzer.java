package com.example.devopsproj.analyzer;

import com.example.devopsproj.engine.HeapSimulationEngine;
import com.example.devopsproj.model.*;
import com.example.devopsproj.repository.GCEventRepository;
import com.example.devopsproj.repository.HeapSnapshotRepository;
import com.example.devopsproj.repository.LeakReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class LeakAnalyzer {

    private final HeapSnapshotRepository heapSnapshotRepository;
    private final GCEventRepository gcEventRepository;
    private final LeakReportRepository leakReportRepository;
    private final HeapSimulationEngine heapSimulationEngine;

    public LeakReport analyzeAndGenerateReport() {
        log.info("Starting leak analysis");

        double heapGrowthRate = calculateHeapGrowthRate();
        double gcEfficiency = calculateGCEfficiency();
        double liveObjectRatio = calculateLiveObjectRatio();
        double oldGenGrowthRate = calculateOldGenGrowthRate();
        double liveObjectStagnation = calculateLiveObjectStagnation();

        double suspicionScore = calculateSuspicionScore(
                heapGrowthRate,
                oldGenGrowthRate,
                gcEfficiency,
                liveObjectStagnation
        );

        Verdict verdict = determineVerdict(suspicionScore);

        String leakingObjectTypes = identifyLeakingObjectTypes();
        Long suspectedLeakDuration = calculateSuspectedLeakDuration();

        LeakReport report = LeakReport.builder()
                .suspicionScore(suspicionScore)
                .leakingObjectTypes(leakingObjectTypes)
                .suspectedLeakDuration(suspectedLeakDuration)
                .heapGrowthRate(heapGrowthRate)
                .gcEfficiency(gcEfficiency)
                .oldGenGrowthRate(oldGenGrowthRate)
                .liveObjectStagnation(liveObjectStagnation)
                .verdict(verdict)
                .createdAt(LocalDateTime.now())
                .build();

        leakReportRepository.save(report);

        log.info("Leak analysis completed. Verdict: {}, Score: {}", verdict, suspicionScore);

        return report;
    }

    public double calculateHeapGrowthRate() {
        Optional<HeapSnapshot> firstSnapshot = heapSnapshotRepository.findFirstSnapshot();
        Optional<HeapSnapshot> latestSnapshot = heapSnapshotRepository.findLatestSnapshot();

        if (firstSnapshot.isEmpty() || latestSnapshot.isEmpty()) {
            return 0.0;
        }

        HeapSnapshot first = firstSnapshot.get();
        HeapSnapshot latest = latestSnapshot.get();

        if (first.getId().equals(latest.getId())) {
            return 0.0;
        }

        long heapDiff = latest.getTotalHeapUsed() - first.getTotalHeapUsed();
        long timeDiffSeconds = Duration.between(first.getTimestamp(), latest.getTimestamp()).getSeconds();

        if (timeDiffSeconds == 0) {
            return 0.0;
        }

        return (double) heapDiff / timeDiffSeconds;
    }

    public double calculateGCEfficiency() {
        Double avgEfficiency = gcEventRepository.calculateAverageGCEfficiency();

        if (avgEfficiency == null) {
            List<GCEvent> recentGCEvents = gcEventRepository.findAllOrderByTimestampDesc();
            if (recentGCEvents.isEmpty()) {
                return 1.0;
            }

            double totalEfficiency = 0.0;
            for (GCEvent event : recentGCEvents) {
                if (event.getBeforeHeap() > 0) {
                    totalEfficiency += (double) event.getReclaimedMemory() / event.getBeforeHeap();
                }
            }
            return recentGCEvents.isEmpty() ? 1.0 : totalEfficiency / recentGCEvents.size();
        }

        return avgEfficiency;
    }

    public double calculateLiveObjectRatio() {
        long liveObjects = heapSimulationEngine.getLiveObjectCount();
        long totalObjects = liveObjects + heapSimulationEngine.getUnreachableObjectCount();

        if (totalObjects == 0) {
            return 0.0;
        }

        return (double) liveObjects / totalObjects;
    }

    public double calculateOldGenGrowthRate() {
        List<HeapSnapshot> snapshots = heapSnapshotRepository.findAllOrderByTimestampDesc();

        if (snapshots.size() < 2) {
            return 0.0;
        }

        snapshots.sort((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()));

        HeapSnapshot first = snapshots.get(0);
        HeapSnapshot last = snapshots.get(snapshots.size() - 1);

        Long firstOldGen = first.getOldGenSize() != null ? first.getOldGenSize() : 0L;
        Long lastOldGen = last.getOldGenSize() != null ? last.getOldGenSize() : 0L;

        long oldGenDiff = lastOldGen - firstOldGen;
        long timeDiffSeconds = Duration.between(first.getTimestamp(), last.getTimestamp()).getSeconds();

        if (timeDiffSeconds == 0) {
            return 0.0;
        }

        return (double) oldGenDiff / timeDiffSeconds;
    }

    public double calculateLiveObjectStagnation() {
        List<HeapSnapshot> snapshots = heapSnapshotRepository.findAllOrderByTimestampDesc();

        if (snapshots.size() < 3) {
            return 0.0;
        }

        snapshots = snapshots.subList(0, Math.min(5, snapshots.size()));

        double avgLiveObjects = snapshots.stream()
                .mapToInt(HeapSnapshot::getLiveObjectsCount)
                .average()
                .orElse(0.0);

        double variance = snapshots.stream()
                .mapToDouble(s -> Math.pow(s.getLiveObjectsCount() - avgLiveObjects, 2))
                .average()
                .orElse(0.0);

        double standardDeviation = Math.sqrt(variance);

        if (avgLiveObjects == 0) {
            return 0.0;
        }

        double coefficientOfVariation = standardDeviation / avgLiveObjects;

        return 1.0 - Math.min(coefficientOfVariation, 1.0);
    }

    public double calculateSuspicionScore(
            double heapGrowthRate,
            double oldGenGrowthRate,
            double gcEfficiency,
            double liveObjectStagnation) {

        double normalizedHeapGrowth = Math.min(Math.abs(heapGrowthRate) / 100.0, 1.0);
        double normalizedOldGenGrowth = Math.min(Math.abs(oldGenGrowthRate) / 100.0, 1.0);
        double gcInefficiency = Math.max(0.0, 1.0 - gcEfficiency);

        double rawScore = (normalizedHeapGrowth * 0.3)
                + (normalizedOldGenGrowth * 0.3)
                + (gcInefficiency * 0.2)
                + (liveObjectStagnation * 0.2);

        return Math.min(Math.max(rawScore * 100, 0.0), 100.0);
    }

    public Verdict determineVerdict(double suspicionScore) {
        if (suspicionScore <= 30) {
            return Verdict.NO_LEAK;
        } else if (suspicionScore <= 60) {
            return Verdict.POSSIBLE_LEAK;
        } else {
            return Verdict.HIGH_PROBABILITY_LEAK;
        }
    }

    public String identifyLeakingObjectTypes() {
        List<HeapObject> liveObjects = heapSimulationEngine.getAllObjects().stream()
                .filter(HeapObject::getReferenced)
                .filter(obj -> obj.getGeneration() == Generation.OLD)
                .toList();

        if (liveObjects.isEmpty()) {
            return "None identified";
        }

        Map<String, Long> objectTypeCounts = liveObjects.stream()
                .collect(Collectors.groupingBy(HeapObject::getObjectType, Collectors.counting()));

        return objectTypeCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    public Long calculateSuspectedLeakDuration() {
        Optional<HeapSnapshot> firstSnapshot = heapSnapshotRepository.findFirstSnapshot();
        Optional<HeapSnapshot> latestSnapshot = heapSnapshotRepository.findLatestSnapshot();

        if (firstSnapshot.isEmpty() || latestSnapshot.isEmpty()) {
            return 0L;
        }

        return Duration.between(
                firstSnapshot.get().getTimestamp(),
                latestSnapshot.get().getTimestamp()
        ).getSeconds();
    }

    public List<LeakReport> getAllReports() {
        return leakReportRepository.findAllOrderByCreatedAtDesc();
    }

    public Optional<LeakReport> getLatestReport() {
        return leakReportRepository.findLatestReport();
    }
}
