package com.example.bookabin.controller;

import com.example.bookabin.entity.User;
import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.entity.BbmpOfficial;
import com.example.bookabin.entity.BBMPSettings;
import com.example.bookabin.repository.UserRepository;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.BbmpOfficialRepository;
import com.example.bookabin.repository.BookingRepository;
import com.example.bookabin.repository.PaymentRepository;
import com.example.bookabin.repository.BBMPSettingsRepository;
import com.example.bookabin.repository.AdminRepository;
import com.example.bookabin.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final CollectorRepository collectorRepository;
    private final BbmpOfficialRepository bbmpOfficialRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final BBMPSettingsRepository bbmpSettingsRepository;
    private final AdminRepository adminRepository;
    private final UserService userService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
            CollectorRepository collectorRepository,
            BbmpOfficialRepository bbmpOfficialRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            BBMPSettingsRepository bbmpSettingsRepository,
            AdminRepository adminRepository,
            UserService userService,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.collectorRepository = collectorRepository;
        this.bbmpOfficialRepository = bbmpOfficialRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.bbmpSettingsRepository = bbmpSettingsRepository;
        this.adminRepository = adminRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/collectors")
    public String listCollectors(Model model) {
        List<GarbageCollector> collectors = collectorRepository.findAll();
        model.addAttribute("collectors", collectors);
        return "admin/collectors";
    }

    @GetMapping("/bookings")
    public String listBookings(Model model) {
        var bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @GetMapping("/payments")
    public String listPayments(Model model) {
        var payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        return "admin/payments";
    }

    @GetMapping("/bbmp")
    public String listBbmp(Model model) {
        List<BbmpOfficial> bbmps = bbmpOfficialRepository.findAll();
        model.addAttribute("bbmps", bbmps);
        return "admin/bbmp";
    }

    @PostMapping("/delete/user/{id}")
    public String deleteUser(@PathVariable Long id) {
        // delete related bookings and payments
        bookingRepository.findByUserId(id).forEach(b -> {
            paymentRepository.findByBookingUser(b.getUser()).forEach(p -> paymentRepository.delete(p));
            bookingRepository.delete(b);
        });
        userRepository.deleteById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/collector/{id}")
    public String deleteCollector(@PathVariable Long id) {
        collectorRepository.deleteById(id);
        return "redirect:/admin/collectors";
    }

    @PostMapping("/delete/bbmp/{id}")
    public String deleteBbmp(@PathVariable Long id) {
        bbmpOfficialRepository.deleteById(id);
        return "redirect:/admin/bbmp";
    }

    @PostMapping("/add/collector")
    public String addCollector(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String phone) {
        GarbageCollector c = new GarbageCollector();
        c.setName(name);
        c.setEmail(email);
        c.setPassword(passwordEncoder.encode(password));
        c.setLocation(location);
        c.setPhoneNumber(phone);
        collectorRepository.save(c);
        return "redirect:/admin/collectors";
    }

    @PostMapping("/update/collector/{id}")
    public String updateCollector(@PathVariable Long id,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String phone) {
        var optional = collectorRepository.findById(id);
        if (optional.isPresent()) {
            GarbageCollector c = optional.get();
            c.setName(name);
            c.setEmail(email);
            if (password != null && !password.isBlank()) {
                c.setPassword(passwordEncoder.encode(password));
            }
            c.setLocation(location);
            c.setPhoneNumber(phone);
            collectorRepository.save(c);
        }
        return "redirect:/admin/collectors";
    }

    @PostMapping("/add/user")
    public String addUser(@RequestParam String username,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone) {
        User u = new User();
        u.setUsername(username);
        u.setName(name);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setPhone(phone);
        u.setRole("USER");
        userRepository.save(u);
        return "redirect:/admin/users";
    }

    @PostMapping("/add/bbmp")
    public String addBbmp(@RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone) {
        BbmpOfficial b = new BbmpOfficial();
        b.setName(name);
        b.setEmail(email);
        // encode password
        b.setPassword(passwordEncoder.encode(password));
        b.setAddress(address);
        b.setPhone(phone);
        bbmpOfficialRepository.save(b);
        return "redirect:/admin/bbmp";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        List<BBMPSettings> list = bbmpSettingsRepository.findAll();
        BBMPSettings setting = list.isEmpty() ? new BBMPSettings() : list.get(0);
        model.addAttribute("settings", setting);
        return "admin/settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@ModelAttribute BBMPSettings settings) {
        bbmpSettingsRepository.save(settings);
        return "redirect:/admin/settings";
    }
}