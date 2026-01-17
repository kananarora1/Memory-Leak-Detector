package com.example.devopsproj.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "leak_report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeakReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double suspicionScore;

    @Column(columnDefinition = "TEXT")
    private String leakingObjectTypes;

    @Column
    private Long suspectedLeakDuration;

    @Column
    private Double heapGrowthRate;

    @Column
    private Double gcEfficiency;

    @Column
    private Double oldGenGrowthRate;

    @Column
    private Double liveObjectStagnation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Verdict verdict;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
