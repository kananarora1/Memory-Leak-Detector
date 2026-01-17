package com.example.devopsproj.dto;

import com.example.devopsproj.model.Verdict;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeakReportResponse {

    private Long id;
    private Double suspicionScore;
    private String leakingObjectTypes;
    private Long suspectedLeakDuration;
    private Double heapGrowthRate;
    private Double gcEfficiency;
    private Double oldGenGrowthRate;
    private Double liveObjectStagnation;
    private Verdict verdict;
    private LocalDateTime createdAt;
    private String summary;
}
