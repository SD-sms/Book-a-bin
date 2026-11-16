// src/main/java/com/example/bookabin/service/BookingService.java
package com.example.bookabin.service;

import com.example.bookabin.entity.Booking;
import com.example.bookabin.repository.BookingRepository;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.BBMPSettingsRepository;
import com.example.bookabin.repository.PaymentRepository;
import com.example.bookabin.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CollectorRepository collectorRepository;
    @Autowired
    private BBMPSettingsRepository bbmpSettingsRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Collector accepts a booking (assign collector and mark Confirmed)
     */
    public Booking confirmBooking(Long bookingId, Long collectorId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        // Prevent multiple collectors confirming the same booking.
        // If the booking is already assigned to a different collector or already
        // confirmed by someone else, do not overwrite.
        if (booking.getCollector() != null) {
            Long existingId = booking.getCollector().getId();
            if (collectorId == null || !existingId.equals(collectorId)) {
                // another collector already owns this booking; do not reassign
                return booking;
            }
            // if same collector re-confirming, allow idempotent behavior
        }

        if (booking.getStatus() != null && booking.getStatus().equalsIgnoreCase("CONFIRMED")
                && (booking.getCollector() == null || !booking.getCollector().getId().equals(collectorId))) {
            // Already confirmed by someone else; do nothing
            return booking;
        }

        if (collectorId != null) {
            collectorRepository.findById(collectorId).ifPresent(booking::setCollector);
        }
        // Use readable status; other code checks are case-insensitive
        booking.setStatus("Confirmed");
        Booking saved = bookingRepository.save(booking);

        // Remove or cancel any duplicate bookings for the same user at the same
        // preferred date/time and location so other collectors cannot confirm the
        // same slot. We will delete pending duplicates to keep the system clean.
        try {
            if (saved.getUser() != null && saved.getPreferredDateTime() != null && saved.getLocation() != null) {
                java.util.List<Booking> others = bookingRepository
                        .findByUserIdAndPreferredDateTimeAndLocationAndIdNot(saved.getUser().getId(),
                                saved.getPreferredDateTime(), saved.getLocation(), saved.getId());
                for (var o : others) {
                    // only delete bookings that are still pending or not yet completed/paid
                    if (o.getStatus() == null || o.getStatus().equalsIgnoreCase("Pending")
                            || o.getStatus().equalsIgnoreCase("Confirmed")) {
                        try {
                            bookingRepository.delete(o);
                        } catch (Exception ex) {
                            // ignore deletion errors
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // ignore duplicate-cleanup errors
        }

        return saved;
    }

    /**
     * Set segregation after collection and apply fine if needed. If not segregated,
     * create a pending payment record for the fine amount.
     */
    public Booking setSegregation(Long bookingId, boolean segregated) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setSegregated(segregated);

        double fineAmount = 0.0;
        try {
            var list = bbmpSettingsRepository.findAll();
            if (!list.isEmpty()) {
                var s = list.get(0);
                if (s.getFinePerViolation() != null)
                    fineAmount = s.getFinePerViolation();
            }
        } catch (Exception e) {
            fineAmount = 200.0;
        }

        if (!segregated) {
            booking.setFine(fineAmount);
            // create a pending payment for the fine so Admin/User can see it
            try {
                Payment p = new Payment();
                p.setAmount(fineAmount);
                p.setBooking(booking);
                p.setUser(booking.getUser());
                p.setPaymentDate(java.time.LocalDateTime.now());
                p.setStatus("Pending");
                p.setPurpose("Fine - Segregation Violation");
                paymentRepository.save(p);
            } catch (Exception ex) {
                // ignore payment creation errors, but booking fine is set
            }
        } else {
            booking.setFine(0.0);
        }
        return bookingRepository.save(booking);
    }

    /**
     * Mark booking as completed after collection
     */
    public Booking completeBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setStatus("COMPLETED");
        return bookingRepository.save(booking);
    }

    /**
     * Reject a booking (set status to REJECTED)
     */
    public Booking rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setStatus("REJECTED");
        return bookingRepository.save(booking);
    }

    public List<Booking> getBookingsForCollector(Long collectorId) {
        return bookingRepository.findByCollectorId(collectorId);
    }

    public List<Booking> getBookingsForUser(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}