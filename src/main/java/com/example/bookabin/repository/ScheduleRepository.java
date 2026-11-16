package com.example.bookabin.repository;

import com.example.bookabin.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByCollectorId(Long collectorId);
}
