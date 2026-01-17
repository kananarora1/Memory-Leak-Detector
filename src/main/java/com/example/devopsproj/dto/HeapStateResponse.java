package com.example.devopsproj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeapStateResponse {

    private LocalDateTime timestamp;
    private Long totalHeapUsed;
    private Integer liveObjectsCount;
    private Integer unreachableObjectsCount;
    private Long youngGenSize;
    private Long oldGenSize;
    private Double heapUtilization;
}
