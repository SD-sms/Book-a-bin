package com.example.bookabin.controller;

import com.example.bookabin.entity.User;
import com.example.bookabin.entity.Admin;
import com.example.bookabin.entity.BbmpOfficial;
import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.service.UserService;
import com.example.bookabin.repository.AdminRepository;
import com.example.bookabin.repository.BbmpOfficialRepository;
import com.example.bookabin.repository.CollectorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class AuthRestController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;
    private final BbmpOfficialRepository bbmpOfficialRepository;
    private final CollectorRepository collectorRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthRestController(UserService userService,
            AuthenticationManager authenticationManager,
            AdminRepository adminRepository,
            BbmpOfficialRepository bbmpOfficialRepository,
            CollectorRepository collectorRepository,
            PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.adminRepository = adminRepository;
        this.bbmpOfficialRepository = bbmpOfficialRepository;
        this.collectorRepository = collectorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/api/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> payload) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String roleIn = ((String) payload.getOrDefault("role", "user")).toLowerCase();
            // Common fields
            String username = (String) payload.get("username");
            if (username == null || username.isBlank())
                username = (String) payload.get("id");
            String email = (String) payload.get("email");
            String rawPwd = (String) payload.get("password");
            String name = (String) payload.get("name");
            String phone = (String) payload.get("phone");
            String address = (String) payload.get("address");

            switch (roleIn) {
                case "user": {
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setName(name);
                    user.setPhone(phone);
                    user.setAddress(address);
                    if (rawPwd != null && !rawPwd.isBlank())
                        user.setPassword(rawPwd);
                    user.setRole("USER");
                    userService.save(user);
                    break;
                }
                case "collector": {
                    GarbageCollector c = new GarbageCollector();
                    c.setName(name != null ? name : username);
                    c.setEmail(email != null ? email : username);
                    c.setPhoneNumber(phone);
                    c.setLocation((String) payload.get("location"));
                    if (rawPwd != null && !rawPwd.isBlank())
                        c.setPassword(passwordEncoder.encode(rawPwd));
                    collectorRepository.save(c);
                    break;
                }
                case "bbmp": {
                    BbmpOfficial b = new BbmpOfficial();
                    b.setName(name != null ? name : username);
                    b.setEmail(email != null ? email : username);
                    b.setPhone(phone);
                    b.setAddress(address);
                    if (rawPwd != null && !rawPwd.isBlank())
                        b.setPassword(passwordEncoder.encode(rawPwd));
                    bbmpOfficialRepository.save(b);
                    break;
                }
                case "admin": {
                    Admin a = new Admin();
                    a.setName(name != null ? name : username);
                    a.setEmail(email != null ? email : username);
                    a.setPhone(phone);
                    a.setAddress(address);
                    if (rawPwd != null && !rawPwd.isBlank())
                        a.setPassword(passwordEncoder.encode(rawPwd));
                    adminRepository.save(a);
                    break;
                }
                default: {
                    // fallback to user
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setName(name);
                    user.setPhone(phone);
                    user.setAddress(address);
                    if (rawPwd != null && !rawPwd.isBlank())
                        user.setPassword(rawPwd);
                    user.setRole(roleIn.toUpperCase());
                    userService.save(user);
                    break;
                }
            }
            resp.put("status", "success");
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.put("status", "fail");
            resp.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload,
            HttpServletRequest request) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String username = payload.getOrDefault("username", payload.get("id"));
            String password = payload.get("password");
            String role = payload.getOrDefault("role", "user");

            // If the frontend sent a display name or id for admin/bbmp/collector, try to
            // resolve to email
            if (username != null && !username.contains("@")) {
                switch (role.toLowerCase()) {
                    case "admin": {
                        // try by email first (already no @), then by name
                        var aOpt = adminRepository.findByName(username);
                        if (aOpt.isPresent()) {
                            username = aOpt.get().getEmail();
                        }
                        break;
                    }
                    case "bbmp": {
                        var bOpt = bbmpOfficialRepository.findByName(username);
                        if (bOpt.isPresent()) {
                            username = bOpt.get().getEmail();
                        }
                        break;
                    }
                    case "collector": {
                        var c = collectorRepository.findByName(username);
                        if (c != null && c.getEmail() != null) {
                            username = c.getEmail();
                        }
                        break;
                    }
                    default:
                        // users are looked up by username; keep as-is
                }
            }

            if (username == null || password == null) {
                resp.put("status", "fail");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
            Authentication authResult = authenticationManager.authenticate(token);

            SecurityContextHolder.getContext().setAuthentication(authResult);
            request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Determine role from authentication authorities
            String roleStr = authResult.getAuthorities().stream()
                    .map(Object::toString)
                    .findFirst()
                    .orElse("ROLE_USER");

            resp.put("status", "success");
            resp.put("role", roleStr);
            resp.put("username", authResult.getName());
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            resp.put("status", "fail");
            resp.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
    }
}