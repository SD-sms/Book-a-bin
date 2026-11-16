package com.example.bookabin.repository;

import com.example.bookabin.entity.GarbageCollector;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectorRepository extends JpaRepository<GarbageCollector, Long> {
    GarbageCollector findByEmail(String email);

    GarbageCollector findByName(String name);

    @SuppressWarnings("unchecked")
    GarbageCollector save(GarbageCollector collector1);
}
