package com.example.devopsproj.controller;

import com.example.devopsproj.dto.ApiResponse;
import com.example.devopsproj.dto.GCEventResponse;
import com.example.devopsproj.dto.LeakReportResponse;
import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.model.HeapSnapshot;
import com.example.devopsproj.model.LeakReport;
import com.example.devopsproj.service.LeakAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/analyze")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analysis", description = "Leak analysis and reporting operations")
public class AnalysisController {

    private final LeakAnalysisService leakAnalysisService;

    @GetMapping("/leak")
    @Operation(summary = "Generate leak report", description = "Analyze heap and generate memory leak detection report")
    public ResponseEntity<ApiResponse<LeakReportResponse>> generateLeakReport() {

        log.info("Controller: Generating leak report");

        LeakReport report = leakAnalysisService.generateLeakReport();

        LeakReportResponse response = mapToResponse(report);

        return ResponseEntity.ok(ApiResponse.success(
                "Leak analysis completed. Verdict: " + report.getVerdict(),
                response
        ));
    }

    @GetMapping("/history")
    @Operation(summary = "Get heap history", description = "Retrieve heap snapshots over time")
    public ResponseEntity<ApiResponse<List<HeapSnapshot>>> getHeapHistory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endTime) {

        log.info("Controller: Getting heap history");

        List<HeapSnapshot> snapshots = leakAnalysisService.getHeapHistory(startTime, endTime);

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + snapshots.size() + " heap snapshots",
                snapshots
        ));
    }

    @GetMapping("/gc-events")
    @Operation(summary = "Get GC events", description = "Retrieve all garbage collection events")
    public ResponseEntity<ApiResponse<List<GCEventResponse>>> getGCEvents() {

        log.info("Controller: Getting GC events");

        List<GCEvent> events = leakAnalysisService.getGCHistory();

        List<GCEventResponse> responses = events.stream()
                .map(this::mapGCEventToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + responses.size() + " GC events",
                responses
        ));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get all reports", description = "Retrieve all leak analysis reports")
    public ResponseEntity<ApiResponse<List<LeakReportResponse>>> getAllReports() {

        log.info("Controller: Getting all leak reports");

        List<LeakReport> reports = leakAnalysisService.getAllReports();

        List<LeakReportResponse> responses = reports.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + responses.size() + " leak reports",
                responses
        ));
    }

    @GetMapping("/latest-report")
    @Operation(summary = "Get latest report", description = "Retrieve the most recent leak analysis report")
    public ResponseEntity<ApiResponse<LeakReportResponse>> getLatestReport() {

        log.info("Controller: Getting latest leak report");

        return leakAnalysisService.getLatestReport()
                .map(report -> ResponseEntity.ok(ApiResponse.success(
                        "Latest leak report retrieved",
                        mapToResponse(report)
                )))
                .orElse(ResponseEntity.ok(ApiResponse.success(
                        "No reports found",
                        null
                )));
    }

    private LeakReportResponse mapToResponse(LeakReport report) {
        String summary = generateSummary(report);

        return LeakReportResponse.builder()
                .id(report.getId())
                .suspicionScore(report.getSuspicionScore())
                .leakingObjectTypes(report.getLeakingObjectTypes())
                .suspectedLeakDuration(report.getSuspectedLeakDuration())
                .heapGrowthRate(report.getHeapGrowthRate())
                .gcEfficiency(report.getGcEfficiency())
                .oldGenGrowthRate(report.getOldGenGrowthRate())
                .liveObjectStagnation(report.getLiveObjectStagnation())
                .verdict(report.getVerdict())
                .createdAt(report.getCreatedAt())
                .summary(summary)
                .build();
    }

    private String generateSummary(LeakReport report) {
        switch (report.getVerdict()) {
            case NO_LEAK:
                return "No memory leak detected. Heap behavior is normal.";
            case POSSIBLE_LEAK:
                return "Possible memory leak detected. Monitor heap growth and GC efficiency.";
            case HIGH_PROBABILITY_LEAK:
                return "High probability of memory leak. Immediate investigation recommended.";
            default:
                return "Unknown verdict.";
        }
    }

    private GCEventResponse mapGCEventToResponse(GCEvent gcEvent) {
        double efficiency = gcEvent.getBeforeHeap() > 0
                ? (double) gcEvent.getReclaimedMemory() / gcEvent.getBeforeHeap()
                : 0.0;

        return GCEventResponse.builder()
                .id(gcEvent.getId())
                .gcType(gcEvent.getGcType())
                .beforeHeap(gcEvent.getBeforeHeap())
                .afterHeap(gcEvent.getAfterHeap())
                .reclaimedMemory(gcEvent.getReclaimedMemory())
                .objectsCollected(gcEvent.getObjectsCollected())
                .timestamp(gcEvent.getTimestamp())
                .efficiency(efficiency)
                .build();
    }
}
