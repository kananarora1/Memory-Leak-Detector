package com.example.devopsproj.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "heap_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeapSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Long totalHeapUsed;

    @Column(nullable = false)
    private Integer liveObjectsCount;

    @Column(nullable = false)
    private Integer unreachableObjectsCount;

    @Column
    private Long youngGenSize;

    @Column
    private Long oldGenSize;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
