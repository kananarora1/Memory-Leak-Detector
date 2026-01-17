package com.example.devopsproj.engine;

import com.example.devopsproj.model.GCEvent;
import com.example.devopsproj.model.GCType;
import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.repository.GCEventRepository;
import com.example.devopsproj.repository.HeapObjectRepository;
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
class GarbageCollectionEngineTest {

    @Mock
    private HeapObjectRepository heapObjectRepository;

    @Mock
    private GCEventRepository gcEventRepository;

    @Mock
    private HeapSimulationEngine heapSimulationEngine;

    @InjectMocks
    private GarbageCollectionEngine garbageCollectionEngine;

    @Test
    void testRunMinorGC_Success() {
        HeapObject unreachableObj = HeapObject.builder()
                .id(1L)
                .sizeInKB(100)
                .referenced(false)
                .generation(Generation.YOUNG)
                .build();

        when(heapSimulationEngine.getCurrentHeapSize()).thenReturn(1000L, 900L);
        when(heapObjectRepository.findUnreachableObjectsByGeneration(Generation.YOUNG))
                .thenReturn(Arrays.asList(unreachableObj));
        when(gcEventRepository.save(any(GCEvent.class)))
                .thenAnswer(i -> i.getArgument(0));

        GCEvent result = garbageCollectionEngine.runMinorGC();

        assertThat(result).isNotNull();
        assertThat(result.getGcType()).isEqualTo(GCType.MINOR);
        assertThat(result.getReclaimedMemory()).isEqualTo(100L);
        verify(heapObjectRepository).deleteAll(anyList());
        verify(heapSimulationEngine).promoteYoungObjects();
    }

    @Test
    void testRunMajorGC_Success() {
        HeapObject unreachableObj = HeapObject.builder()
                .id(1L)
                .sizeInKB(200)
                .referenced(false)
                .generation(Generation.OLD)
                .build();

        when(heapSimulationEngine.getCurrentHeapSize()).thenReturn(2000L, 1800L);
        when(heapObjectRepository.findUnreachableObjectsByGeneration(Generation.OLD))
                .thenReturn(Arrays.asList(unreachableObj));
        when(gcEventRepository.save(any(GCEvent.class)))
                .thenAnswer(i -> i.getArgument(0));

        GCEvent result = garbageCollectionEngine.runMajorGC();

        assertThat(result).isNotNull();
        assertThat(result.getGcType()).isEqualTo(GCType.MAJOR);
        assertThat(result.getReclaimedMemory()).isEqualTo(200L);
        verify(heapObjectRepository).deleteAll(anyList());
    }

    @Test
    void testGetAverageGCEfficiency() {
        when(gcEventRepository.calculateAverageGCEfficiency()).thenReturn(0.75);

        Double efficiency = garbageCollectionEngine.getAverageGCEfficiency();

        assertThat(efficiency).isEqualTo(0.75);
    }
}
