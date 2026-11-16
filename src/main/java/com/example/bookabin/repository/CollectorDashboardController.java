package com.example.bookabin.repository;

import com.example.bookabin.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CollectorDashboardController extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.collector.id = :collectorId AND b.status = 'PENDING'")
    List<Booking> findPendingByCollectorId(@Param("collectorId") long collectorId);

    @Query("SELECT b FROM Booking b WHERE b.collector.id = :collectorId AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedByCollectorId(@Param("collectorId") long collectorId);
}