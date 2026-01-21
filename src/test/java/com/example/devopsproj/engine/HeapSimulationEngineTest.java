package com.example.devopsproj.engine;

import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import com.example.devopsproj.repository.HeapObjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HeapSimulationEngineTest {

    @Mock
    private HeapObjectRepository heapObjectRepository;

    @InjectMocks
    private HeapSimulationEngine heapSimulationEngine;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(heapSimulationEngine, "youngGenAgeThreshold", 10);
        ReflectionTestUtils.setField(heapSimulationEngine, "maxHeapSizeKB", 1048576L);
    }

    @Test
    void testAllocateObjects_Success() {
        HeapObject mockObject = HeapObject.builder()
                .id(1L)
                .sizeInKB(100)
                .objectType("TestObject")
                .referenced(true)
                .generation(Generation.YOUNG)
                .age(0)
                .build();

        when(heapObjectRepository.save(any(HeapObject.class))).thenReturn(mockObject);

        List<HeapObject> result = heapSimulationEngine.allocateObjects(5, 100, "TestObject", true);

        assertThat(result).hasSize(5);
        verify(heapObjectRepository, times(5)).save(any(HeapObject.class));
    }

    @Test
    void testAllocateRandomObjects_Success() {
        HeapObject mockObject = HeapObject.builder()
                .id(1L)
                .sizeInKB(50)
                .objectType("RandomObject")
                .referenced(true)
                .generation(Generation.YOUNG)
                .build();

        when(heapObjectRepository.save(any(HeapObject.class))).thenReturn(mockObject);

        List<HeapObject> result = heapSimulationEngine.allocateRandomObjects(3, 10, 100, "RandomObject");

        assertThat(result).hasSize(3);
        verify(heapObjectRepository, times(3)).save(any(HeapObject.class));
    }

    @Test
    void testDereferenceObjects_Success() {
        heapSimulationEngine.dereferenceObjects(Arrays.asList(1L, 2L));

        verify(heapObjectRepository, times(0)).save(any(HeapObject.class));
    }

    @Test
    void testPromoteYoungObjects_Success() {
        HeapObject youngObject = HeapObject.builder()
                .id(1L)
                .generation(Generation.YOUNG)
                .age(15)
                .build();

        when(heapObjectRepository.findByGeneration(Generation.YOUNG))
                .thenReturn(Arrays.asList(youngObject));
        when(heapObjectRepository.save(any(HeapObject.class))).thenReturn(youngObject);

        heapSimulationEngine.promoteYoungObjects();

        verify(heapObjectRepository).save(any(HeapObject.class));
    }

    @Test
    void testGetCurrentHeapSize_Success() {
        when(heapObjectRepository.calculateTotalHeapUsed()).thenReturn(5000L);

        long heapSize = heapSimulationEngine.getCurrentHeapSize();

        assertThat(heapSize).isEqualTo(5000L);
    }

    @Test
    void testGetCurrentHeapSize_NullReturnsZero() {
        when(heapObjectRepository.calculateTotalHeapUsed()).thenReturn(null);

        long heapSize = heapSimulationEngine.getCurrentHeapSize();

        assertThat(heapSize).isEqualTo(0L);
    }

    @Test
    void testGetLiveObjectCount() {
        when(heapObjectRepository.countLiveObjects()).thenReturn(10L);

        long count = heapSimulationEngine.getLiveObjectCount();

        assertThat(count).isEqualTo(10L);
    }

    @Test
    void testClearHeap() {
        heapSimulationEngine.clearHeap();

        verify(heapObjectRepository).deleteAll();
    }
}
