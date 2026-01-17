package com.example.devopsproj.controller;

import com.example.devopsproj.dto.ApiResponse;
import com.example.devopsproj.dto.GCEventResponse;
import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.service.GarbageCollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/simulate/gc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Garbage Collection", description = "Garbage collection simulation operations")
public class GarbageCollectionController {

    private final GarbageCollectionService garbageCollectionService;

    @PostMapping("/minor")
    @Operation(summary = "Run Minor GC", description = "Execute Minor GC (Young Generation)")
    public ResponseEntity<ApiResponse<GCEventResponse>> runMinorGC() {

        log.info("Controller: Running Minor GC");

        GCEvent gcEvent = garbageCollectionService.runMinorGC();

        GCEventResponse response = mapToResponse(gcEvent);

        return ResponseEntity.ok(ApiResponse.success(
                "Minor GC completed successfully",
                response
        ));
    }

    @PostMapping("/major")
    @Operation(summary = "Run Major GC", description = "Execute Major GC (Old Generation)")
    public ResponseEntity<ApiResponse<GCEventResponse>> runMajorGC() {

        log.info("Controller: Running Major GC");

        GCEvent gcEvent = garbageCollectionService.runMajorGC();

        GCEventResponse response = mapToResponse(gcEvent);

        return ResponseEntity.ok(ApiResponse.success(
                "Major GC completed successfully",
                response
        ));
    }

    @PostMapping("/full")
    @Operation(summary = "Run Full GC", description = "Execute Full GC (Both Generations)")
    public ResponseEntity<ApiResponse<GCEventResponse>> runFullGC() {

        log.info("Controller: Running Full GC");

        GCEvent gcEvent = garbageCollectionService.runFullGC();

        GCEventResponse response = mapToResponse(gcEvent);

        return ResponseEntity.ok(ApiResponse.success(
                "Full GC completed successfully",
                response
        ));
    }

    @GetMapping("/history")
    @Operation(summary = "Get GC history", description = "Retrieve history of all GC events")
    public ResponseEntity<ApiResponse<List<GCEventResponse>>> getGCHistory() {

        log.info("Controller: Getting GC history");

        List<GCEvent> events = garbageCollectionService.getGCHistory();

        List<GCEventResponse> responses = events.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + responses.size() + " GC events",
                responses
        ));
    }

    private GCEventResponse mapToResponse(GCEvent gcEvent) {
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
