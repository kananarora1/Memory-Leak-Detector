package com.example.devopsproj.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gc_event")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GCEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GCType gcType;

    @Column(nullable = false)
    private Long beforeHeap;

    @Column(nullable = false)
    private Long afterHeap;

    @Column(nullable = false)
    private Long reclaimedMemory;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column
    private Integer objectsCollected;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (reclaimedMemory == null && beforeHeap != null && afterHeap != null) {
            reclaimedMemory = beforeHeap - afterHeap;
        }
    }
}
