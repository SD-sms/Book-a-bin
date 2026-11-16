package com.example.bookabin.service;

import com.example.bookabin.entity.Admin;
import com.example.bookabin.entity.BbmpOfficial;
import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.entity.User;
import com.example.bookabin.repository.AdminRepository;
import com.example.bookabin.repository.BbmpOfficialRepository;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepo;
    private final BbmpOfficialRepository bbmpRepo;
    private final CollectorRepository collectorRepo;
    private final UserRepository userRepo;

    public CustomUserDetailsService(AdminRepository adminRepo,
            BbmpOfficialRepository bbmpRepo,
            CollectorRepository collectorRepo,
            UserRepository userRepo) {
        this.adminRepo = adminRepo;
        this.bbmpRepo = bbmpRepo;
        this.collectorRepo = collectorRepo;
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // try admins by email
        Optional<Admin> admin = adminRepo.findByEmail(username);
        if (admin.isPresent()) {
            Admin a = admin.get();
            List<GrantedAuthority> auth = new ArrayList<>();
            auth.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new org.springframework.security.core.userdetails.User(a.getEmail(), a.getPassword(), auth);
        }

        // try BBMP officials by email
        Optional<BbmpOfficial> bbmp = bbmpRepo.findByEmail(username);
        if (bbmp.isPresent()) {
            BbmpOfficial b = bbmp.get();
            List<GrantedAuthority> auth = new ArrayList<>();
            auth.add(new SimpleGrantedAuthority("ROLE_BBMP_COLLECTOR"));
            return new org.springframework.security.core.userdetails.User(b.getEmail(), b.getPassword(), auth);
        }

        // try collectors by email
        GarbageCollector collector = collectorRepo.findByEmail(username);
        if (collector != null) {
            List<GrantedAuthority> auth = new ArrayList<>();
            auth.add(new SimpleGrantedAuthority("ROLE_COLLECTOR"));
            return new org.springframework.security.core.userdetails.User(collector.getEmail(), collector.getPassword(),
                    auth);
        }

        // finally try users by username
        Optional<User> user = userRepo.findByUsername(username);
        if (user.isPresent()) {
            User u = user.get();
            List<GrantedAuthority> auth = new ArrayList<>();
            String role = u.getRole();
            if (role == null || role.isBlank())
                role = "ROLE_USER";
            if (!role.startsWith("ROLE_"))
                role = "ROLE_" + role;
            auth.add(new SimpleGrantedAuthority(role));
            return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), auth);
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}