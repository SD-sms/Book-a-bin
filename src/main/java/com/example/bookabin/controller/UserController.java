package com.example.bookabin.controller;

import com.example.bookabin.entity.Booking;
import com.example.bookabin.entity.User;
import com.example.bookabin.service.BookingService;
import com.example.bookabin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/book")
    public Booking createBooking(@RequestBody Booking booking, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        booking.setUser(user);
        booking.setStatus("Pending");
        return bookingService.saveBooking(booking);
    }

    @GetMapping("/bookings")
    public List<Booking> getBookings(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return bookingService.getBookingsForUser(user.getId());
    }
    
}