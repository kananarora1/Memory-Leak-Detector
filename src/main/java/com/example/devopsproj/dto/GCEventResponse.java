package com.example.devopsproj.dto;

import com.example.devopsproj.model.GCType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GCEventResponse {

    private Long id;
    private GCType gcType;
    private Long beforeHeap;
    private Long afterHeap;
    private Long reclaimedMemory;
    private Integer objectsCollected;
    private LocalDateTime timestamp;
    private Double efficiency;
}
