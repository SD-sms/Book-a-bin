package com.example.bookabin.controller;

import com.example.bookabin.entity.Booking;
import com.example.bookabin.entity.Payment;
import com.example.bookabin.entity.User;
import com.example.bookabin.repository.BookingRepository;
import com.example.bookabin.repository.PaymentRepository;
import com.example.bookabin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Controller
public class UserDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private com.example.bookabin.service.UserService userService;
    @Autowired
    private com.example.bookabin.repository.BBMPSettingsRepository bbmpSettingsRepository;
    @Autowired
    private com.example.bookabin.service.FixedFeeService fixedFeeService;
    @Autowired
    private com.example.bookabin.repository.ScheduleRepository scheduleRepository;
    @Autowired
    private com.example.bookabin.repository.CollectorRepository collectorRepository;

    @GetMapping("/user/dashboard")
    public String dashboard(Principal principal, Model model) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);

            List<Booking> bookings;
            if (user == null) {
                bookings = Collections.emptyList();
            } else {
                bookings = bookingRepository.findAll()
                        .stream()
                        .filter(b -> b.getUser() != null && user.getId().equals(b.getUser().getId()))
                        .collect(Collectors.toList());
            }
            model.addAttribute("bookings", bookings);

            List<Payment> payments;
            if (user == null) {
                payments = Collections.emptyList();
            } else {
                payments = paymentRepository.findAll()
                        .stream()
                        .filter(p -> p.getUser() != null && user.getId().equals(p.getUser().getId()))
                        .collect(Collectors.toList());
            }
            model.addAttribute("payments", payments);
        }
        return "user/dashboard";
    }

    @GetMapping(value = "/user/book-bin", produces = "text/html")
    public String bookBinPage(Principal principal, Model model) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        // Add empty booking object so the form's th:object binding works
        model.addAttribute("booking", new Booking());
        return "user/book-bin";
    }

    @PostMapping("/user/book-bin")
    public String submitBooking(@ModelAttribute Booking booking, Principal principal) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            booking.setUser(user);
        }
        // default status when user creates a booking
        booking.setStatus("Pending");
        // Set booking amount from BBMP settings if available, otherwise from FixedFee
        try {
            java.util.List<com.example.bookabin.entity.BBMPSettings> list = bbmpSettingsRepository.findAll();
            if (!list.isEmpty()) {
                com.example.bookabin.entity.BBMPSettings s = list.get(0);
                if (s.getCollectionFee() != null) {
                    booking.setAmount(s.getCollectionFee());
                }
            } else {
                // fallback to legacy FixedFee
                var fee = fixedFeeService.getLatestFee();
                if (fee != null)
                    booking.setAmount(fee.getAmount());
            }
        } catch (Exception ex) {
            // ignore and leave amount null
        }
        // Do NOT auto-assign a collector at booking creation. Leave the booking
        // unassigned so all collectors in the area can see it and one can claim it.
        // Collector assignment will happen when a collector confirms the booking.

        bookingRepository.save(booking);
        return "redirect:/user/bookings";
    }

    @GetMapping(value = "/user/make-payment", produces = "text/html")
    public String makePaymentPage(Principal principal, Model model) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
            // attach user's bookings for selecting during payment
            java.util.List<Booking> bookings = java.util.Collections.emptyList();
            if (user != null) {
                bookings = bookingRepository.findAll().stream()
                        .filter(b -> b.getUser() != null && user.getId().equals(b.getUser().getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("bookings", bookings);
        }
        return "user/make-payment";
    }

    @GetMapping(value = "/user/bookings", produces = "text/html")
    public String userBookingsPage(Principal principal, Model model) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
            java.util.List<Booking> bookings = java.util.Collections.emptyList();
            if (user != null) {
                bookings = bookingRepository.findAll().stream()
                        .filter(b -> b.getUser() != null && user.getId().equals(b.getUser().getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("bookings", bookings);
        }
        return "user/bookings";
    }

    @GetMapping(value = "/user/schedules", produces = "text/html")
    public String userSchedulesPage(Principal principal, Model model) {
        // show published schedules (all)
        try {
            java.util.List<com.example.bookabin.entity.Schedule> schedules = scheduleRepository.findAll();
            model.addAttribute("schedules", schedules);
        } catch (Exception ex) {
            model.addAttribute("schedules", java.util.Collections.emptyList());
        }
        return "user/schedules";
    }

    @GetMapping(value = "/user/payments", produces = "text/html")
    public String userPaymentsPage(Principal principal, Model model) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
            java.util.List<Payment> payments = java.util.Collections.emptyList();
            if (user != null) {
                payments = paymentRepository.findAll().stream()
                        .filter(p -> p.getUser() != null && user.getId().equals(p.getUser().getId()))
                        .collect(java.util.stream.Collectors.toList());
            }
            model.addAttribute("payments", payments);
        }
        return "user/payments";
    }

    @GetMapping(value = "/user/edit-profile", produces = "text/html")
    public String editProfilePage(Principal principal, Model model) {
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            model.addAttribute("user", user);
        }
        return "user/edit-profile";
    }

    @PostMapping("/user/update-profile")
    public String updateProfile(Principal principal,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String details,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword) {
        if (principal == null) {
            return "redirect:/login";
        }
        var optUser = userRepository.findByUsername(principal.getName());
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }
        var user = optUser.get();
        if (name != null)
            user.setName(name);
        if (email != null)
            user.setEmail(email);
        if (phone != null)
            user.setPhone(phone);
        if (address != null)
            user.setAddress(address);
        if (details != null)
            user.setDetails(details);

        // handle optional password change
        if (password != null && !password.isBlank()) {
            if (confirmPassword != null && password.equals(confirmPassword)) {
                user.setPassword(password);
                // use UserService.save to encode password and normalize role
                userService.save(user);
            } else {
                // passwords didn't match: do not change password but save other fields
                userRepository.save(user);
            }
        } else {
            // no password change requested
            // use updateUserProfile to only update profile fields and preserve password
            userService.updateUserProfile(user.getId(), user);
        }

        return "redirect:/user/edit-profile?success";
    }

    @PostMapping("/user/make-payment")
    public String processPayment(@RequestParam Long bookingId,
            @RequestParam String paymentMethod,
            Principal principal) {
        var booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return "redirect:/user/make-payment?error";
        }

        var payment = new Payment();
        double amount = (booking.getAmount() != null ? booking.getAmount() : 0)
                + (booking.getFine() != null ? booking.getFine() : 0);
        payment.setAmount(amount);
        payment.setBooking(booking);
        if (principal != null) {
            var user = userRepository.findByUsername(principal.getName()).orElse(null);
            payment.setUser(user);
        }
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("Success");
        payment.setTransactionId(java.util.UUID.randomUUID().toString());
        payment.setPurpose("Booking Payment");
        paymentRepository.save(payment);

        booking.setStatus("Paid");
        bookingRepository.save(booking);

        return "redirect:/user/payments";
    }

    @PostMapping("/user/delete-booking/{id}")
    public String deleteBooking(@PathVariable Long id, Principal principal) {
        var opt = bookingRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/user/bookings?notfound";
        }
        var booking = opt.get();
        if (principal == null) {
            return "redirect:/login";
        }
        var user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user == null || booking.getUser() == null || !user.getId().equals(booking.getUser().getId())) {
            return "redirect:/user/bookings?forbidden";
        }
        // Only allow deleting bookings that are still Pending (not
        // confirmed/paid/completed)
        if (booking.getStatus() != null && !booking.getStatus().equalsIgnoreCase("Pending")) {
            return "redirect:/user/bookings?cannotdelete";
        }
        bookingRepository.deleteById(id);
        return "redirect:/user/bookings?deleted";
    }
}