package com.example.devopsproj.service;

import com.example.devopsproj.engine.HeapSimulationEngine;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.model.HeapSnapshot;
import com.example.devopsproj.repository.HeapSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

    @Mock
    private HeapSimulationEngine heapSimulationEngine;

    @Mock
    private HeapSnapshotRepository heapSnapshotRepository;

    @InjectMocks
    private SimulationService simulationService;

    @Test
    void testAllocateObjects_Success() {
        HeapObject mockObject = HeapObject.builder().id(1L).build();

        when(heapSimulationEngine.allocateObjects(anyInt(), anyInt(), anyString(), anyBoolean()))
                .thenReturn(Arrays.asList(mockObject));

        List<HeapObject> result = simulationService.allocateObjects(5, 100, "Test", true);

        assertThat(result).hasSize(1);
        verify(heapSimulationEngine).allocateObjects(5, 100, "Test", true);
    }

    @Test
    void testDereferenceObjects_Success() {
        List<Long> objectIds = Arrays.asList(1L, 2L);

        simulationService.dereferenceObjects(objectIds);

        verify(heapSimulationEngine).dereferenceObjects(objectIds);
    }

    @Test
    void testGetCurrentHeapState_Success() {
        when(heapSimulationEngine.getCurrentHeapSize()).thenReturn(1000L);
        when(heapSimulationEngine.getLiveObjectCount()).thenReturn(50L);
        when(heapSimulationEngine.getUnreachableObjectCount()).thenReturn(10L);
        when(heapSimulationEngine.getYoungGenSize()).thenReturn(500L);
        when(heapSimulationEngine.getOldGenSize()).thenReturn(500L);

        HeapSnapshot snapshot = simulationService.getCurrentHeapState();

        assertThat(snapshot).isNotNull();
        assertThat(snapshot.getTotalHeapUsed()).isEqualTo(1000L);
        assertThat(snapshot.getLiveObjectsCount()).isEqualTo(50);
    }

    @Test
    void testCreateHeapSnapshot_Success() {
        when(heapSimulationEngine.getCurrentHeapSize()).thenReturn(1000L);
        when(heapSimulationEngine.getLiveObjectCount()).thenReturn(50L);
        when(heapSimulationEngine.getUnreachableObjectCount()).thenReturn(10L);
        when(heapSnapshotRepository.save(any(HeapSnapshot.class)))
                .thenAnswer(i -> i.getArgument(0));

        HeapSnapshot snapshot = simulationService.createHeapSnapshot();

        assertThat(snapshot).isNotNull();
        verify(heapSnapshotRepository).save(any(HeapSnapshot.class));
    }

    @Test
    void testClearHeap_Success() {
        simulationService.clearHeap();

        verify(heapSimulationEngine).clearHeap();
    }
}
