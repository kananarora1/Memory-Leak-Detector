package com.example.devopsproj.repository;

import com.example.devopsproj.model.Generation;
import com.example.devopsproj.model.HeapObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeapObjectRepository extends JpaRepository<HeapObject, Long> {

    List<HeapObject> findByReferenced(Boolean referenced);

    List<HeapObject> findByGeneration(Generation generation);

    @Query("SELECT h FROM HeapObject h WHERE h.referenced = false")
    List<HeapObject> findUnreachableObjects();

    @Query("SELECT h FROM HeapObject h WHERE h.referenced = true")
    List<HeapObject> findLiveObjects();

    @Query("SELECT COUNT(h) FROM HeapObject h WHERE h.referenced = true")
    long countLiveObjects();

    @Query("SELECT COUNT(h) FROM HeapObject h WHERE h.referenced = false")
    long countUnreachableObjects();

    @Query("SELECT SUM(h.sizeInKB) FROM HeapObject h WHERE h.referenced = true")
    Long calculateTotalHeapUsed();

    @Query("SELECT SUM(h.sizeInKB) FROM HeapObject h WHERE h.generation = :generation")
    Long calculateGenerationSize(Generation generation);

    @Query("SELECT h FROM HeapObject h WHERE h.generation = :generation AND h.referenced = false")
    List<HeapObject> findUnreachableObjectsByGeneration(Generation generation);

    void deleteByReferenced(Boolean referenced);
}
