package com.example.bookabin.repository;

import com.example.bookabin.entity.Booking;
import com.example.bookabin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCollectorId(Long collectorId);

    // find other bookings for same user at same preferred date/time and location
    java.util.List<Booking> findByUserIdAndPreferredDateTimeAndLocationAndIdNot(Long userId,
            java.time.LocalDateTime preferredDateTime, String location, Long id);
}
