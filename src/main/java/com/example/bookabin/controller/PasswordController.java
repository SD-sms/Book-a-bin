package com.example.bookabin.controller;

import com.example.bookabin.entity.Admin;
import com.example.bookabin.entity.BbmpOfficial;
import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.entity.User;
import com.example.bookabin.repository.AdminRepository;
import com.example.bookabin.repository.BbmpOfficialRepository;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PasswordController {

    private final UserRepository userRepository;
    private final CollectorRepository collectorRepository;
    private final BbmpOfficialRepository bbmpOfficialRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordController(UserRepository userRepository,
            CollectorRepository collectorRepository,
            BbmpOfficialRepository bbmpOfficialRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.collectorRepository = collectorRepository;
        this.bbmpOfficialRepository = bbmpOfficialRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model, @RequestParam(required = false) String role) {
        model.addAttribute("role", role == null ? "" : role);
        return "forgot-password";
    }

    @PostMapping("/api/forgot-password")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam String role,
            @RequestParam String identifier,
            @RequestParam String newPassword) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String r = role == null ? "user" : role.toLowerCase();
            switch (r) {
                case "admin": {
                    adminRepository.findByEmail(identifier).ifPresent(a -> {
                        a.setPassword(passwordEncoder.encode(newPassword));
                        adminRepository.save(a);
                    });
                    break;
                }
                case "bbmp": {
                    bbmpOfficialRepository.findByEmail(identifier).ifPresent(b -> {
                        b.setPassword(passwordEncoder.encode(newPassword));
                        bbmpOfficialRepository.save(b);
                    });
                    break;
                }
                case "collector": {
                    GarbageCollector c = collectorRepository.findByEmail(identifier);
                    if (c != null) {
                        c.setPassword(passwordEncoder.encode(newPassword));
                        collectorRepository.save(c);
                    }
                    break;
                }
                default: {
                    // user: identifier is username or email
                    User user = userRepository.findByUsername(identifier).orElse(null);
                    if (user == null) {
                        // try email
                        user = userRepository.findAll().stream()
                                .filter(u -> identifier.equalsIgnoreCase(u.getEmail()))
                                .findFirst().orElse(null);
                    }
                    if (user != null) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                    }
                    break;
                }
            }
            resp.put("status", "success");
        } catch (Exception ex) {
            resp.put("status", "fail");
            resp.put("error", ex.getMessage());
        }
        return resp;
    }
}
