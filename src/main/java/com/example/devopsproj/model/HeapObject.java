package com.example.devopsproj.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "heap_object")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeapObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer sizeInKB;

    @Column(nullable = false)
    private LocalDateTime allocationTime;

    @Column
    private LocalDateTime lastAccessTime;

    @Column(nullable = false)
    private Boolean referenced;

    @Column(nullable = false)
    private String objectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Generation generation;

    @Column
    private Integer age;

    @PrePersist
    protected void onCreate() {
        if (allocationTime == null) {
            allocationTime = LocalDateTime.now();
        }
        if (referenced == null) {
            referenced = true;
        }
        if (generation == null) {
            generation = Generation.YOUNG;
        }
        if (age == null) {
            age = 0;
        }
    }
}
