package com.example.devopsproj.analyzer;

import com.example.devopsproj.engine.HeapSimulationEngine;
import com.example.devopsproj.model.*;
import com.example.devopsproj.repository.GCEventRepository;
import com.example.devopsproj.repository.HeapSnapshotRepository;
import com.example.devopsproj.repository.LeakReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeakAnalyzerTest {

    @Mock
    private HeapSnapshotRepository heapSnapshotRepository;

    @Mock
    private GCEventRepository gcEventRepository;

    @Mock
    private LeakReportRepository leakReportRepository;

    @Mock
    private HeapSimulationEngine heapSimulationEngine;

    @InjectMocks
    private LeakAnalyzer leakAnalyzer;

    @Test
    void testCalculateHeapGrowthRate_WithSnapshots() {
        HeapSnapshot first = HeapSnapshot.builder()
                .id(1L)
                .totalHeapUsed(1000L)
                .timestamp(LocalDateTime.now().minusSeconds(100))
                .build();

        HeapSnapshot latest = HeapSnapshot.builder()
                .id(2L)
                .totalHeapUsed(2000L)
                .timestamp(LocalDateTime.now())
                .build();

        when(heapSnapshotRepository.findFirstSnapshot()).thenReturn(Optional.of(first));
        when(heapSnapshotRepository.findLatestSnapshot()).thenReturn(Optional.of(latest));

        double growthRate = leakAnalyzer.calculateHeapGrowthRate();

        assertThat(growthRate).isGreaterThan(0);
    }

    @Test
    void testCalculateHeapGrowthRate_NoSnapshots() {
        when(heapSnapshotRepository.findFirstSnapshot()).thenReturn(Optional.empty());
        when(heapSnapshotRepository.findLatestSnapshot()).thenReturn(Optional.empty());

        double growthRate = leakAnalyzer.calculateHeapGrowthRate();

        assertThat(growthRate).isEqualTo(0.0);
    }

    @Test
    void testCalculateGCEfficiency_WithEvents() {
        when(gcEventRepository.calculateAverageGCEfficiency()).thenReturn(0.8);

        double efficiency = leakAnalyzer.calculateGCEfficiency();

        assertThat(efficiency).isEqualTo(0.8);
    }

    @Test
    void testCalculateLiveObjectRatio() {
        when(heapSimulationEngine.getLiveObjectCount()).thenReturn(80L);
        when(heapSimulationEngine.getUnreachableObjectCount()).thenReturn(20L);

        double ratio = leakAnalyzer.calculateLiveObjectRatio();

        assertThat(ratio).isEqualTo(0.8);
    }

    @Test
    void testCalculateLiveObjectRatio_NoObjects() {
        when(heapSimulationEngine.getLiveObjectCount()).thenReturn(0L);
        when(heapSimulationEngine.getUnreachableObjectCount()).thenReturn(0L);

        double ratio = leakAnalyzer.calculateLiveObjectRatio();

        assertThat(ratio).isEqualTo(0.0);
    }

    @Test
    void testCalculateSuspicionScore() {
        double score = leakAnalyzer.calculateSuspicionScore(50.0, 30.0, 0.5, 0.7);

        assertThat(score).isBetween(0.0, 100.0);
    }

    @Test
    void testDetermineVerdict_NoLeak() {
        Verdict verdict = leakAnalyzer.determineVerdict(25.0);

        assertThat(verdict).isEqualTo(Verdict.NO_LEAK);
    }

    @Test
    void testDetermineVerdict_PossibleLeak() {
        Verdict verdict = leakAnalyzer.determineVerdict(45.0);

        assertThat(verdict).isEqualTo(Verdict.POSSIBLE_LEAK);
    }

    @Test
    void testDetermineVerdict_HighProbabilityLeak() {
        Verdict verdict = leakAnalyzer.determineVerdict(75.0);

        assertThat(verdict).isEqualTo(Verdict.HIGH_PROBABILITY_LEAK);
    }

    @Test
    void testAnalyzeAndGenerateReport() {
        HeapSnapshot snapshot = HeapSnapshot.builder()
                .id(1L)
                .totalHeapUsed(1000L)
                .timestamp(LocalDateTime.now())
                .build();

        when(heapSnapshotRepository.findFirstSnapshot()).thenReturn(Optional.of(snapshot));
        when(heapSnapshotRepository.findLatestSnapshot()).thenReturn(Optional.of(snapshot));
        when(gcEventRepository.calculateAverageGCEfficiency()).thenReturn(0.8);
        when(heapSimulationEngine.getLiveObjectCount()).thenReturn(50L);
        when(heapSimulationEngine.getUnreachableObjectCount()).thenReturn(10L);
        when(heapSimulationEngine.getAllObjects()).thenReturn(Arrays.asList());
        when(heapSnapshotRepository.findAllOrderByTimestampDesc()).thenReturn(Arrays.asList());
        when(leakReportRepository.save(any(LeakReport.class)))
                .thenAnswer(i -> i.getArgument(0));

        LeakReport report = leakAnalyzer.analyzeAndGenerateReport();

        assertThat(report).isNotNull();
        assertThat(report.getVerdict()).isIn(Verdict.NO_LEAK, Verdict.POSSIBLE_LEAK, Verdict.HIGH_PROBABILITY_LEAK);
        verify(leakReportRepository).save(any(LeakReport.class));
    }
}
