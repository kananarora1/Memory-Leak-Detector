package com.example.devopsproj.engine;

import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.repository.HeapObjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class HeapSimulationEngine {

    private final Map<Long, HeapObject> inMemoryHeap;
    private final AtomicLong objectIdCounter;
    private final HeapObjectRepository heapObjectRepository;

    @Value("${simulation.max-heap-size-kb:1048576}")
    private Long maxHeapSizeKB;

    @Value("${simulation.young-gen-threshold:10}")
    private Integer youngGenAgeThreshold;

    public HeapSimulationEngine(HeapObjectRepository heapObjectRepository) {
        this.heapObjectRepository = heapObjectRepository;
        this.inMemoryHeap = new ConcurrentHashMap<>();
        this.objectIdCounter = new AtomicLong(0);
    }

    public List<HeapObject> allocateObjects(int count, int sizeInKB, String objectType, boolean createReferences) {
        List<HeapObject> allocatedObjects = new ArrayList<>();

        log.info("Allocating {} objects of size {} KB and type {}", count, sizeInKB, objectType);

        for (int i = 0; i < count; i++) {
            HeapObject heapObject = HeapObject.builder()
                    .sizeInKB(sizeInKB)
                    .allocationTime(LocalDateTime.now())
                    .lastAccessTime(LocalDateTime.now())
                    .referenced(createReferences)
                    .objectType(objectType)
                    .generation(Generation.YOUNG)
                    .age(0)
                    .build();

            HeapObject savedObject = heapObjectRepository.save(heapObject);
            inMemoryHeap.put(savedObject.getId(), savedObject);
            allocatedObjects.add(savedObject);
        }

        log.info("Successfully allocated {} objects. Current heap size: {} KB",
                count, getCurrentHeapSize());

        return allocatedObjects;
    }

    public List<HeapObject> allocateRandomObjects(int count, int minSize, int maxSize, String objectType) {
        List<HeapObject> allocatedObjects = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            int size = random.nextInt(maxSize - minSize + 1) + minSize;
            HeapObject heapObject = HeapObject.builder()
                    .sizeInKB(size)
                    .allocationTime(LocalDateTime.now())
                    .lastAccessTime(LocalDateTime.now())
                    .referenced(true)
                    .objectType(objectType)
                    .generation(Generation.YOUNG)
                    .age(0)
                    .build();

            HeapObject savedObject = heapObjectRepository.save(heapObject);
            inMemoryHeap.put(savedObject.getId(), savedObject);
            allocatedObjects.add(savedObject);
        }

        return allocatedObjects;
    }

    public void dereferenceObjects(List<Long> objectIds) {
        log.info("Dereferencing {} objects", objectIds.size());

        for (Long objectId : objectIds) {
            HeapObject heapObject = inMemoryHeap.get(objectId);
            if (heapObject != null) {
                heapObject.setReferenced(false);
                heapObjectRepository.save(heapObject);
                inMemoryHeap.put(objectId, heapObject);
            }
        }
    }

    public void promoteYoungObjects() {
        log.info("Promoting old young generation objects to old generation");

        List<HeapObject> youngObjects = heapObjectRepository.findByGeneration(Generation.YOUNG);
        int promotedCount = 0;

        for (HeapObject obj : youngObjects) {
            obj.setAge(obj.getAge() + 1);

            if (obj.getAge() >= youngGenAgeThreshold) {
                obj.setGeneration(Generation.OLD);
                promotedCount++;
                log.debug("Promoted object {} to OLD generation", obj.getId());
            }

            heapObjectRepository.save(obj);
            inMemoryHeap.put(obj.getId(), obj);
        }

        log.info("Promoted {} objects to OLD generation", promotedCount);
    }

    public long getCurrentHeapSize() {
        Long totalSize = heapObjectRepository.calculateTotalHeapUsed();
        return totalSize != null ? totalSize : 0L;
    }

    public long getYoungGenSize() {
        Long size = heapObjectRepository.calculateGenerationSize(Generation.YOUNG);
        return size != null ? size : 0L;
    }

    public long getOldGenSize() {
        Long size = heapObjectRepository.calculateGenerationSize(Generation.OLD);
        return size != null ? size : 0L;
    }

    public long getLiveObjectCount() {
        return heapObjectRepository.countLiveObjects();
    }

    public long getUnreachableObjectCount() {
        return heapObjectRepository.countUnreachableObjects();
    }

    public List<HeapObject> getAllObjects() {
        return heapObjectRepository.findAll();
    }

    public void refreshInMemoryHeap() {
        inMemoryHeap.clear();
        List<HeapObject> allObjects = heapObjectRepository.findAll();
        for (HeapObject obj : allObjects) {
            inMemoryHeap.put(obj.getId(), obj);
        }
    }

    public void clearHeap() {
        log.info("Clearing entire heap");
        heapObjectRepository.deleteAll();
        inMemoryHeap.clear();
    }
}
