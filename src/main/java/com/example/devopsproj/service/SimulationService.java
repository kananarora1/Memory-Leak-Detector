package com.example.devopsproj.service;

import com.example.devopsproj.engine.HeapSimulationEngine;
import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.model.HeapSnapshot;
import com.example.devopsproj.repository.HeapSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationService {

    private final HeapSimulationEngine heapSimulationEngine;
    private final HeapSnapshotRepository heapSnapshotRepository;

    @Transactional
    public List<HeapObject> allocateObjects(int count, int sizeInKB, String objectType, boolean createReferences) {
        log.info("Service: Allocating {} objects", count);
        return heapSimulationEngine.allocateObjects(count, sizeInKB, objectType, createReferences);
    }

    @Transactional
    public List<HeapObject> allocateRandomObjects(int count, int minSize, int maxSize, String objectType) {
        log.info("Service: Allocating {} random objects", count);
        return heapSimulationEngine.allocateRandomObjects(count, minSize, maxSize, objectType);
    }

    @Transactional
    public void dereferenceObjects(List<Long> objectIds) {
        log.info("Service: Dereferencing {} objects", objectIds.size());
        heapSimulationEngine.dereferenceObjects(objectIds);
    }

    public HeapSnapshot getCurrentHeapState() {
        log.info("Service: Getting current heap state");

        long totalHeapUsed = heapSimulationEngine.getCurrentHeapSize();
        int liveObjectsCount = (int) heapSimulationEngine.getLiveObjectCount();
        int unreachableObjectsCount = (int) heapSimulationEngine.getUnreachableObjectCount();
        long youngGenSize = heapSimulationEngine.getYoungGenSize();
        long oldGenSize = heapSimulationEngine.getOldGenSize();

        return HeapSnapshot.builder()
                .timestamp(LocalDateTime.now())
                .totalHeapUsed(totalHeapUsed)
                .liveObjectsCount(liveObjectsCount)
                .unreachableObjectsCount(unreachableObjectsCount)
                .youngGenSize(youngGenSize)
                .oldGenSize(oldGenSize)
                .build();
    }

    @Transactional
    public HeapSnapshot createHeapSnapshot() {
        log.info("Service: Creating heap snapshot");

        HeapSnapshot snapshot = getCurrentHeapState();
        return heapSnapshotRepository.save(snapshot);
    }

    public List<HeapSnapshot> getHeapHistory(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return heapSnapshotRepository.findAllOrderByTimestampDesc();
        }
        return heapSnapshotRepository.findByTimestampBetween(startTime, endTime);
    }

    public List<HeapObject> getAllObjects() {
        return heapSimulationEngine.getAllObjects();
    }

    @Transactional
    public void clearHeap() {
        log.info("Service: Clearing heap");
        heapSimulationEngine.clearHeap();
    }

    @Transactional
    public int dereferenceRandomObjects(int percentage) {
        log.info("Service: Dereferencing random {}% of objects", percentage);

        List<HeapObject> allObjects = heapSimulationEngine.getAllObjects();
        int totalCount = allObjects.size();
        int dereferenceCount = (int) Math.ceil(totalCount * percentage / 100.0);

        List<Long> randomIds = allObjects.stream()
                .filter(HeapObject::getReferenced)
                .limit(dereferenceCount)
                .map(HeapObject::getId)
                .toList();

        heapSimulationEngine.dereferenceObjects(randomIds);

        log.info("Dereferenced {} out of {} objects", randomIds.size(), totalCount);
        return randomIds.size();
    }

    @Transactional
    public List<HeapObject> allocateCyclicReferences(int chainLength, int sizeInKB) {
        log.info("Service: Creating cyclic reference chain of length {}", chainLength);

        List<HeapObject> cyclicObjects = heapSimulationEngine.allocateObjects(
                chainLength,
                sizeInKB,
                "CyclicReference",
                true
        );

        log.info("Created circular reference chain: {} objects forming a leak pattern", chainLength);

        return cyclicObjects;
    }
}
