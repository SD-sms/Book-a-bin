// src/main/java/com/example/bookabin/controller/BBMPController.java
package com.example.bookabin.controller;

import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.entity.FixedFee;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.FixedFeeRepository;
import com.example.bookabin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/bbmp")
public class BBMPController {

    @Autowired
    private CollectorRepository collectorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FixedFeeRepository fixedFeeRepository;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired
    private com.example.bookabin.repository.BBMPSettingsRepository bbmpSettingsRepository;
    @Autowired
    private com.example.bookabin.repository.ScheduleRepository scheduleRepository;

    @GetMapping("/dashboard")
    public String bbmpDashboard() {
        return "bbmp/dashboard";
    }

    @GetMapping("/collectors")
    public String viewCollectorsAndUsers(Model model) {
        model.addAttribute("collectors", collectorRepository.findAll());
        model.addAttribute("users", userRepository.findAll());
        return "bbmp/collectors";
    }

    @GetMapping("/users")
    public String viewUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "bbmp/users";
    }

    @GetMapping("/set-fee")
    public String showSetFeePage(Model model) {
        java.util.List<com.example.bookabin.entity.BBMPSettings> list = bbmpSettingsRepository.findAll();
        double current = 0.0;
        if (!list.isEmpty() && list.get(0).getCollectionFee() != null)
            current = list.get(0).getCollectionFee();
        model.addAttribute("currentFee", current);
        return "bbmp/set-fee";
    }

    @PostMapping("/set-fee")
    public String updateFee(@RequestParam double amount) {
        // save/update BBMPSettings collection fee
        java.util.List<com.example.bookabin.entity.BBMPSettings> list = bbmpSettingsRepository.findAll();
        if (list.isEmpty()) {
            com.example.bookabin.entity.BBMPSettings s = new com.example.bookabin.entity.BBMPSettings();
            s.setCollectionFee(amount);
            bbmpSettingsRepository.save(s);
        } else {
            var s = list.get(0);
            s.setCollectionFee(amount);
            bbmpSettingsRepository.save(s);
        }
        return "redirect:/bbmp/dashboard";
    }

    @PostMapping("/set-fine")
    public String updateFine(@RequestParam double fineAmount) {
        java.util.List<com.example.bookabin.entity.BBMPSettings> list = bbmpSettingsRepository.findAll();
        if (list.isEmpty()) {
            com.example.bookabin.entity.BBMPSettings s = new com.example.bookabin.entity.BBMPSettings();
            s.setFinePerViolation(fineAmount);
            bbmpSettingsRepository.save(s);
        } else {
            var s = list.get(0);
            s.setFinePerViolation(fineAmount);
            bbmpSettingsRepository.save(s);
        }
        return "redirect:/bbmp/dashboard";
    }

    @PostMapping("/schedules/publish")
    public String publishSchedule(@RequestParam(name = "collectorId", required = false) String collectorIdParam,
            @RequestParam String area,
            @RequestParam String date,
            @RequestParam String time) {
        com.example.bookabin.entity.Schedule sch = new com.example.bookabin.entity.Schedule();
        Long collectorId = null;

        // collectorIdParam can be empty, a numeric id, a collector name or an email.
        if (collectorIdParam != null && !collectorIdParam.isBlank()) {
            String trimmed = collectorIdParam.trim();
            // try parse as numeric id first
            try {
                Long id = Long.parseLong(trimmed);
                // verify existence
                if (collectorRepository.existsById(id)) {
                    collectorId = id;
                }
            } catch (NumberFormatException nfe) {
                // not numeric - try to resolve by name or email
                var byName = collectorRepository.findByName(trimmed);
                if (byName != null) {
                    collectorId = byName.getId();
                } else {
                    var byEmail = collectorRepository.findByEmail(trimmed);
                    if (byEmail != null) {
                        collectorId = byEmail.getId();
                    }
                }
            }
        }

        // if collectorId remains null we still allow publishing a schedule without an
        // assigned collector.
        sch.setCollectorId(collectorId);
        sch.setArea(area);
        try {
            sch.setDate(java.time.LocalDate.parse(date));
        } catch (Exception ex) {
            sch.setDate(java.time.LocalDate.now());
        }
        sch.setTime(time);
        scheduleRepository.save(sch);
        return "redirect:/bbmp/dashboard";
    }

    @PostMapping("/collectors/add")
    public String addCollector(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phoneNumber,
            @RequestParam String location) {
        GarbageCollector collector = new GarbageCollector();
        collector.setName(username);
        collector.setEmail(email);
        // encode password using configured PasswordEncoder
        collector.setPassword(passwordEncoder.encode(password));
        collector.setPhoneNumber(phoneNumber);
        collector.setLocation(location);
        collectorRepository.save(collector);
        return "redirect:/bbmp/collectors";
    }

    @PostMapping("/collectors/delete/{id}")
    public String deleteCollector(@PathVariable Long id) {
        collectorRepository.deleteById(id);
        return "redirect:/bbmp/collectors";
    }

    @PostMapping("/collectors/update-location/{id}")
    public String updateCollectorLocation(@PathVariable Long id,
            @RequestParam String location) {
        Optional<GarbageCollector> optional = collectorRepository.findById(id);
        optional.ifPresent(collector -> {
            collector.setLocation(location);
            collectorRepository.save(collector);
        });
        return "redirect:/bbmp/collectors";
    }

    // keep this controller and method path unique to avoid duplicate mapping errors
    @GetMapping("/home")
    public String bbmpHome() {
        // redirect to the dashboard template which currently lives at
        // templates/bbmp/dashboard.html
        return "bbmp/dashboard";
    }

    // other BBMP pages (set-fee, users, collectors, dashboard alternatives) can be
    // mapped here
    @GetMapping("/manage")
    public String manage() {
        return "bbmp/dashboard"; // use a different method name/path if needed
    }
}