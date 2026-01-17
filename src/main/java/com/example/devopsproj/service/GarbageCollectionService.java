package com.example.devopsproj.service;

import com.example.devopsproj.engine.GarbageCollectionEngine;
import com.example.devopsproj.model.GCEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GarbageCollectionService {

    private final GarbageCollectionEngine garbageCollectionEngine;
    private final SimulationService simulationService;

    @Transactional
    public GCEvent runMinorGC() {
        log.info("Service: Running Minor GC");
        GCEvent gcEvent = garbageCollectionEngine.runMinorGC();

        simulationService.createHeapSnapshot();

        return gcEvent;
    }

    @Transactional
    public GCEvent runMajorGC() {
        log.info("Service: Running Major GC");
        GCEvent gcEvent = garbageCollectionEngine.runMajorGC();

        simulationService.createHeapSnapshot();

        return gcEvent;
    }

    @Transactional
    public GCEvent runFullGC() {
        log.info("Service: Running Full GC");
        GCEvent gcEvent = garbageCollectionEngine.runFullGC();

        simulationService.createHeapSnapshot();

        return gcEvent;
    }

    public List<GCEvent> getGCHistory() {
        return garbageCollectionEngine.getGCHistory();
    }

    public Double getAverageGCEfficiency() {
        return garbageCollectionEngine.getAverageGCEfficiency();
    }
}
