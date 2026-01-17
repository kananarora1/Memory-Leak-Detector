package com.example.devopsproj.controller;

import com.example.devopsproj.dto.AllocateRequest;
import com.example.devopsproj.dto.ApiResponse;
import com.example.devopsproj.dto.DereferenceRequest;
import com.example.devopsproj.dto.HeapStateResponse;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.model.HeapSnapshot;
import com.example.devopsproj.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/simulate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Simulation", description = "Heap simulation operations")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping("/allocate")
    @Operation(summary = "Allocate objects", description = "Allocate specified number of objects to the heap")
    public ResponseEntity<ApiResponse<List<HeapObject>>> allocateObjects(
            @Valid @RequestBody AllocateRequest request) {

        log.info("Controller: Allocating {} objects of type {}", request.getCount(), request.getObjectType());

        List<HeapObject> allocatedObjects = simulationService.allocateObjects(
                request.getCount(),
                request.getSizeInKB(),
                request.getObjectType(),
                request.getCreateReferences()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Successfully allocated " + allocatedObjects.size() + " objects",
                allocatedObjects
        ));
    }

    @PostMapping("/dereference")
    @Operation(summary = "Dereference objects", description = "Remove references from specified objects")
    public ResponseEntity<ApiResponse<String>> dereferenceObjects(
            @Valid @RequestBody DereferenceRequest request) {

        log.info("Controller: Dereferencing {} objects", request.getObjectIds().size());

        simulationService.dereferenceObjects(request.getObjectIds());

        return ResponseEntity.ok(ApiResponse.success(
                "Successfully dereferenced " + request.getObjectIds().size() + " objects",
                null
        ));
    }

    @GetMapping("/heap")
    @Operation(summary = "Get heap state", description = "Get current heap state and statistics")
    public ResponseEntity<ApiResponse<HeapStateResponse>> getHeapState() {

        log.info("Controller: Getting heap state");

        HeapSnapshot snapshot = simulationService.getCurrentHeapState();

        HeapStateResponse response = HeapStateResponse.builder()
                .timestamp(snapshot.getTimestamp())
                .totalHeapUsed(snapshot.getTotalHeapUsed())
                .liveObjectsCount(snapshot.getLiveObjectsCount())
                .unreachableObjectsCount(snapshot.getUnreachableObjectsCount())
                .youngGenSize(snapshot.getYoungGenSize())
                .oldGenSize(snapshot.getOldGenSize())
                .build();

        return ResponseEntity.ok(ApiResponse.success(
                "Current heap state retrieved successfully",
                response
        ));
    }

    @GetMapping("/objects")
    @Operation(summary = "Get all objects", description = "Retrieve all objects in the heap")
    public ResponseEntity<ApiResponse<List<HeapObject>>> getAllObjects() {

        log.info("Controller: Getting all objects");

        List<HeapObject> objects = simulationService.getAllObjects();

        return ResponseEntity.ok(ApiResponse.success(
                "Retrieved " + objects.size() + " objects",
                objects
        ));
    }

    @PostMapping("/snapshot")
    @Operation(summary = "Create snapshot", description = "Create and persist a heap snapshot")
    public ResponseEntity<ApiResponse<HeapSnapshot>> createSnapshot() {

        log.info("Controller: Creating heap snapshot");

        HeapSnapshot snapshot = simulationService.createHeapSnapshot();

        return ResponseEntity.ok(ApiResponse.success(
                "Heap snapshot created successfully",
                snapshot
        ));
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear heap", description = "Clear all objects from the heap")
    public ResponseEntity<ApiResponse<String>> clearHeap() {

        log.info("Controller: Clearing heap");

        simulationService.clearHeap();

        return ResponseEntity.ok(ApiResponse.success(
                "Heap cleared successfully",
                null
        ));
    }

    @PostMapping("/dereference/random")
    @Operation(summary = "Random dereference", description = "Randomly dereference a percentage of objects")
    public ResponseEntity<ApiResponse<Integer>> dereferenceRandom(
            @RequestParam(defaultValue = "10") Integer percentage) {

        log.info("Controller: Randomly dereferencing {}% of objects", percentage);

        int dereferencedCount = simulationService.dereferenceRandomObjects(percentage);

        return ResponseEntity.ok(ApiResponse.success(
                "Randomly dereferenced " + dereferencedCount + " objects (" + percentage + "%)",
                dereferencedCount
        ));
    }

    @PostMapping("/allocate/cyclic")
    @Operation(summary = "Create cyclic references", description = "Create circular reference chain to simulate memory leak")
    public ResponseEntity<ApiResponse<List<HeapObject>>> allocateCyclic(
            @RequestParam(defaultValue = "5") Integer chainLength,
            @RequestParam(defaultValue = "50") Integer sizeInKB) {

        log.info("Controller: Creating cyclic reference chain of length {}", chainLength);

        List<HeapObject> cyclicObjects = simulationService.allocateCyclicReferences(chainLength, sizeInKB);

        return ResponseEntity.ok(ApiResponse.success(
                "Created circular reference chain of " + chainLength + " objects",
                cyclicObjects
        ));
    }
}
