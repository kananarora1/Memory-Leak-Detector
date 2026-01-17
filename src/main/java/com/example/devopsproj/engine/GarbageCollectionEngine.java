package com.example.devopsproj.engine;

import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.model.GCType;
import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.repository.GCEventRepository;
import com.example.devopsproj.repository.HeapObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GarbageCollectionEngine {

    private final HeapObjectRepository heapObjectRepository;
    private final GCEventRepository gcEventRepository;
    private final HeapSimulationEngine heapSimulationEngine;

    @Transactional
    public GCEvent runMinorGC() {
        log.info("Running Minor GC (Young Generation)");

        long beforeHeap = heapSimulationEngine.getCurrentHeapSize();
        long beforeCount = heapObjectRepository.count();

        List<HeapObject> unreachableYoungObjects =
                heapObjectRepository.findUnreachableObjectsByGeneration(Generation.YOUNG);

        long reclaimedMemory = unreachableYoungObjects.stream()
                .mapToLong(HeapObject::getSizeInKB)
                .sum();

        int objectsCollected = unreachableYoungObjects.size();

        heapObjectRepository.deleteAll(unreachableYoungObjects);

        heapSimulationEngine.promoteYoungObjects();

        long afterHeap = heapSimulationEngine.getCurrentHeapSize();

        GCEvent gcEvent = GCEvent.builder()
                .gcType(GCType.MINOR)
                .beforeHeap(beforeHeap)
                .afterHeap(afterHeap)
                .reclaimedMemory(reclaimedMemory)
                .objectsCollected(objectsCollected)
                .timestamp(LocalDateTime.now())
                .build();

        gcEventRepository.save(gcEvent);

        log.info("Minor GC completed. Reclaimed: {} KB, Objects collected: {}, Heap: {} KB -> {} KB",
                reclaimedMemory, objectsCollected, beforeHeap, afterHeap);

        return gcEvent;
    }

    @Transactional
    public GCEvent runMajorGC() {
        log.info("Running Major GC (Old Generation)");

        long beforeHeap = heapSimulationEngine.getCurrentHeapSize();

        List<HeapObject> unreachableOldObjects =
                heapObjectRepository.findUnreachableObjectsByGeneration(Generation.OLD);

        long reclaimedMemory = unreachableOldObjects.stream()
                .mapToLong(HeapObject::getSizeInKB)
                .sum();

        int objectsCollected = unreachableOldObjects.size();

        heapObjectRepository.deleteAll(unreachableOldObjects);

        long afterHeap = heapSimulationEngine.getCurrentHeapSize();

        GCEvent gcEvent = GCEvent.builder()
                .gcType(GCType.MAJOR)
                .beforeHeap(beforeHeap)
                .afterHeap(afterHeap)
                .reclaimedMemory(reclaimedMemory)
                .objectsCollected(objectsCollected)
                .timestamp(LocalDateTime.now())
                .build();

        gcEventRepository.save(gcEvent);

        log.info("Major GC completed. Reclaimed: {} KB, Objects collected: {}, Heap: {} KB -> {} KB",
                reclaimedMemory, objectsCollected, beforeHeap, afterHeap);

        return gcEvent;
    }

    @Transactional
    public GCEvent runFullGC() {
        log.info("Running Full GC (Both Generations)");

        long beforeHeap = heapSimulationEngine.getCurrentHeapSize();

        List<HeapObject> unreachableObjects = heapObjectRepository.findUnreachableObjects();

        long reclaimedMemory = unreachableObjects.stream()
                .mapToLong(HeapObject::getSizeInKB)
                .sum();

        int objectsCollected = unreachableObjects.size();

        heapObjectRepository.deleteAll(unreachableObjects);

        heapSimulationEngine.promoteYoungObjects();

        long afterHeap = heapSimulationEngine.getCurrentHeapSize();

        GCEvent gcEvent = GCEvent.builder()
                .gcType(GCType.MAJOR)
                .beforeHeap(beforeHeap)
                .afterHeap(afterHeap)
                .reclaimedMemory(reclaimedMemory)
                .objectsCollected(objectsCollected)
                .timestamp(LocalDateTime.now())
                .build();

        gcEventRepository.save(gcEvent);

        log.info("Full GC completed. Reclaimed: {} KB, Objects collected: {}, Heap: {} KB -> {} KB",
                reclaimedMemory, objectsCollected, beforeHeap, afterHeap);

        return gcEvent;
    }

    public List<GCEvent> getGCHistory() {
        return gcEventRepository.findAllOrderByTimestampDesc();
    }

    public Double getAverageGCEfficiency() {
        return gcEventRepository.calculateAverageGCEfficiency();
    }
}
