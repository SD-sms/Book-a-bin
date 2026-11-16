package com.example.bookabin.controller;

import com.example.bookabin.entity.*;
import com.example.bookabin.service.BookingService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Example for controller files
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/collector")
public class CollectorController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private com.example.bookabin.repository.CollectorRepository collectorRepository;
    @Autowired
    private com.example.bookabin.repository.ScheduleRepository scheduleRepository;

    @GetMapping("/bookings")
    public String viewBookings(Model model, Principal principal) {
        // Resolve collector id by email (principal name is email)
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
        }
        List<Booking> bookings = collectorId == null ? java.util.Collections.emptyList()
                : bookingService.getBookingsForCollector(collectorId);
        model.addAttribute("bookings", bookings);
        return "collector/bookings"; // HTML file inside templates/collector/
    }

    @PostMapping("/confirm-booking/{id}")
    public String confirmBooking(@PathVariable Long id,
            @RequestParam(value = "segregated", required = false, defaultValue = "true") boolean segregated,
            Principal principal) {
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
        }
        // Confirm (assign collector & mark Confirmed)
        bookingService.confirmBooking(id, collectorId);
        // Persist segregation choice (true/false) immediately so collector's action
        // is saved and appropriate fines/payments are created when not segregated.
        bookingService.setSegregation(id, segregated);
        // After confirmation and segregation decision, mark the booking completed so
        // it no longer appears repeatedly in the pending/confirmed lists.
        bookingService.completeBooking(id);
        return "redirect:/collector/bookings";
    }

    // Alias endpoints to match expected routes
    @PostMapping("/confirm/{id}")
    public String confirmBookingAlias(@PathVariable Long id,
            @RequestParam(value = "segregated", defaultValue = "true") boolean segregated,
            Principal principal) {
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
        }
        bookingService.confirmBooking(id, collectorId);
        bookingService.setSegregation(id, segregated);
        bookingService.completeBooking(id);
        return "redirect:/collector/bookings";
    }

    @PostMapping("/segregation/{id}")
    public String setSegregation(@PathVariable Long id,
            @RequestParam(value = "segregated", defaultValue = "false") boolean segregated,
            Principal principal) {
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
        }
        // set segregation status and create fine payment if needed
        bookingService.setSegregation(id, segregated);
        return "redirect:/collector/bookings";
    }

    @PostMapping("/complete/{id}")
    public String markComplete(@PathVariable Long id, Principal principal) {
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
        }
        // mark completed without changing segregation flag
        bookingService.completeBooking(id);
        return "redirect:/collector/bookings";
    }

    @PostMapping("/reject/{id}")
    public String rejectBooking(@PathVariable Long id, Principal principal) {
        // mark booking as rejected
        bookingService.rejectBooking(id);
        return "redirect:/collector/bookings";
    }

    @GetMapping("/booking-details/{id}")
    public String bookingDetails(@PathVariable Long id, Model model, Principal principal) {
        var opt = bookingService.getAllBookings().stream().filter(b -> b.getId() != null && b.getId().equals(id))
                .findFirst();
        if (opt.isEmpty()) {
            return "redirect:/collector/bookings?notfound";
        }
        var booking = opt.get();
        model.addAttribute("booking", booking);
        model.addAttribute("user", booking.getUser());
        // collector - if principal present, populate collector for profile block
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            model.addAttribute("collector", coll);
        }
        return "collector/booking-details";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Long collectorId = null;
        if (principal != null) {
            var coll = collectorRepository.findByEmail(principal.getName());
            if (coll != null)
                collectorId = coll.getId();
            model.addAttribute("collector", coll);
        }
        java.util.List<Booking> pending = java.util.Collections.emptyList();
        java.util.List<Booking> confirmed = java.util.Collections.emptyList();
        if (collectorId != null) {
            var allForCollector = bookingService.getBookingsForCollector(collectorId);
            // Include both explicitly confirmed and immediately completed bookings so
            // the collector can see bookings they have handled (some flows mark a
            // booking as COMPLETED right after confirmation to avoid reappearing in
            // pending lists).
            confirmed = allForCollector.stream().filter(b -> {
                String s = b.getStatus();
                return s != null && (s.equalsIgnoreCase("Confirmed") || s.equalsIgnoreCase("Completed")
                        || s.equalsIgnoreCase("COMPLETED"));
            }).toList();
            // Start with bookings already explicitly assigned to this collector
            var assignedPending = allForCollector.stream().filter(b -> "Pending".equalsIgnoreCase(b.getStatus()))
                    .toList();

            // Also include unassigned bookings in the collector's area so the collector can
            // claim/confirm them
            var coll = collectorRepository.findById(collectorId).orElse(null);
            java.util.List<Booking> nearbyUnassigned = java.util.Collections.emptyList();
            if (coll != null && coll.getLocation() != null) {
                String collLoc = coll.getLocation().trim().toLowerCase();
                nearbyUnassigned = bookingService.getAllBookings().stream()
                        .filter(b -> b.getCollector() == null)
                        .filter(b -> b.getLocation() != null)
                        .filter(b -> {
                            String bLoc = b.getLocation().trim().toLowerCase();
                            return collLoc.equals(bLoc) || collLoc.contains(bLoc) || bLoc.contains(collLoc)
                                    || collLoc.startsWith(bLoc) || bLoc.startsWith(collLoc);
                        }).toList();
            }

            // Combine assigned pending and nearby unassigned (avoid duplicates)
            java.util.Set<Long> ids = new java.util.HashSet<>();
            java.util.List<Booking> combined = new java.util.ArrayList<>();
            for (var b : assignedPending) {
                if (b.getId() != null)
                    ids.add(b.getId());
                combined.add(b);
            }
            for (var b : nearbyUnassigned) {
                if (b.getId() == null || !ids.contains(b.getId())) {
                    combined.add(b);
                }
            }
            pending = combined;
        }
        model.addAttribute("pendingBookings", pending);
        model.addAttribute("confirmedBookings", confirmed);
        // schedules for this collector
        java.util.List<com.example.bookabin.entity.Schedule> schedules = collectorId == null
                ? java.util.Collections.emptyList()
                : scheduleRepository.findByCollectorId(collectorId);
        model.addAttribute("schedules", schedules);
        return "collector/dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null)
            return "redirect:/login";
        var coll = collectorRepository.findByEmail(principal.getName());
        model.addAttribute("collector", coll);
        return "collector/profile";
    }

    @GetMapping("/public/{id}")
    public String publicProfile(@PathVariable Long id, Model model) {
        var optional = collectorRepository.findById(id);
        if (optional.isEmpty()) {
            return "redirect:/user/dashboard";
        }
        model.addAttribute("collector", optional.get());
        return "collector/public-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Principal principal,
            @RequestParam String name,
            @RequestParam String phoneNumber,
            @RequestParam String location) {
        if (principal == null)
            return "redirect:/login";
        var coll = collectorRepository.findByEmail(principal.getName());
        if (coll != null) {
            coll.setName(name);
            coll.setPhoneNumber(phoneNumber);
            coll.setLocation(location);
            collectorRepository.save(coll);
        }
        return "redirect:/collector/profile?success";
    }
}
