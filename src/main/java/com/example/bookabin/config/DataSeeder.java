package com.example.bookabin.config;

import com.example.bookabin.entity.Admin;
import com.example.bookabin.entity.BbmpOfficial;
import com.example.bookabin.entity.GarbageCollector;
import com.example.bookabin.entity.BBMPSettings;
import com.example.bookabin.entity.Schedule;
import com.example.bookabin.repository.AdminRepository;
import com.example.bookabin.repository.CollectorRepository;
import com.example.bookabin.repository.BbmpOfficialRepository;
import com.example.bookabin.repository.FixedFeeRepository;
import com.example.bookabin.repository.BBMPSettingsRepository;
import com.example.bookabin.repository.ScheduleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.bookabin.entity.User;
import com.example.bookabin.entity.Booking;
import com.example.bookabin.service.BookingService;
import com.example.bookabin.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seedData(AdminRepository adminRepo,
            CollectorRepository collectorRepo,
            BbmpOfficialRepository bbmpRepo,
            FixedFeeRepository fixedFeeRepo,
            BBMPSettingsRepository bbmpSettingsRepo,
            ScheduleRepository scheduleRepo,
            UserRepository userRepository,
            BookingService bookingService,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed 4 admins
            for (int i = 1; i <= 4; i++) {
                final String email = "admin" + i + "@bbmp.local";
                if (adminRepo.findByEmail(email).isEmpty()) {
                    Admin a = new Admin();
                    a.setName("BBMP Admin " + i);
                    a.setEmail(email);
                    a.setPassword(passwordEncoder.encode("Admin@123"));
                    a.setPhone("000-000-000" + i);
                    a.setAddress("BBMP Office");
                    adminRepo.save(a);
                }
            }

            // Remove collectors that look unencoded or known-bad entries (e.g. "Collector
            // One")
            try {
                for (var existing : collectorRepo.findAll()) {
                    String pw = existing.getPassword();
                    String name = existing.getName() == null ? "" : existing.getName();
                    boolean looksUnencoded = pw == null || !(pw.startsWith("$2a$") || pw.startsWith("$2b$")
                            || pw.startsWith("$2y$") || pw.startsWith("$2"));
                    boolean matchesBadName = name.equalsIgnoreCase("Collector One")
                            || name.equalsIgnoreCase("CollectorOne");
                    if (looksUnencoded || matchesBadName) {
                        try {
                            collectorRepo.delete(existing);
                        } catch (Exception ex) {
                            // ignore deletion errors and continue
                        }
                    }
                }
            } catch (Exception ex) {
                // ignore scan errors
            }

            for (int i = 1; i <= 2; i++) {
                final String email = "collector" + i + "@bookabin.local";
                if (collectorRepo.findByEmail(email) == null) {
                    GarbageCollector c = new GarbageCollector();
                    c.setName("Collector " + i);
                    c.setEmail(email);
                    c.setPassword(passwordEncoder.encode("Collector@123"));
                    c.setPhoneNumber("90000000" + i);
                    c.setLocation("Zone " + i);
                    collectorRepo.save(c);
                }
            }

            // Seed a BBMP official sample (optional)
            final String bbmpEmail = "bbmp1@bbmp.local";
            if (bbmpRepo.findByEmail(bbmpEmail).isEmpty()) {
                BbmpOfficial b = new BbmpOfficial();
                b.setName("BBMP Officer 1");
                b.setEmail(bbmpEmail);
                b.setPassword(passwordEncoder.encode("Bbmp@123"));
                b.setPhone("8888888888");
                b.setAddress("BBMP HQ");
                bbmpRepo.save(b);
            }

            // Seed BBMP settings if none
            if (fixedFeeRepo.count() == 0) {
                // reuse FixedFee as legacy table for collection fee
                fixedFeeRepo.save(new com.example.bookabin.entity.FixedFee(50.0));
            }

            // Seed BBMPSettings if missing (collection fee + fine)
            try {
                if (bbmpSettingsRepo.count() == 0) {
                    BBMPSettings s = new BBMPSettings();
                    s.setCollectionFee(50.0);
                    s.setFinePerViolation(200.0);
                    bbmpSettingsRepo.save(s);
                }
            } catch (Exception ex) {
                // repository may not exist in some branches — ignore to keep seeder safe
            }

            // Seed a sample schedule if none exist
            try {
                if (scheduleRepo.count() == 0) {
                    // associate with first collector if available
                    var coll = collectorRepo.findByEmail("collector1@bookabin.local");
                    Long collId = null;
                    if (coll != null && coll.getId() != null)
                        collId = coll.getId();

                    Schedule sch = new Schedule();
                    sch.setCollectorId(collId);
                    sch.setArea("Zone 1");
                    sch.setDate(LocalDate.now().plusDays(1));
                    sch.setTime("09:00");
                    scheduleRepo.save(sch);
                }
            } catch (Exception ex) {
                // ignore schedule seeding issues
            }

            // --- Quick smoke-test seed: create a test user and a booking, then simulate
            // collector confirm + segregation=false
            try {
                if (userRepository.findByUsername("testuser").isEmpty()) {
                    User u = new User();
                    u.setUsername("testuser");
                    u.setName("Test User");
                    u.setEmail("testuser@example.local");
                    u.setPassword(passwordEncoder.encode("Test@123"));
                    u.setPhone("7000000000");
                    u.setRole("USER");
                    userRepository.save(u);
                }

                var user = userRepository.findByUsername("testuser").orElse(null);
                if (user != null) {
                    Booking b = new Booking();
                    b.setUser(user);
                    b.setBookingDate(LocalDateTime.now());
                    b.setPreferredDateTime(LocalDateTime.now().plusDays(1));
                    b.setLocation("Test Location");
                    b.setStatus("Pending");
                    // set amount from BBMP settings if present
                    double amt = 50.0;
                    try {
                        var list = bbmpSettingsRepo.findAll();
                        if (!list.isEmpty() && list.get(0).getCollectionFee() != null)
                            amt = list.get(0).getCollectionFee();
                    } catch (Exception ign) {
                    }
                    b.setAmount(amt);
                    Booking saved = bookingService.saveBooking(b);

                    // assign to collector1 and mark confirmed then mark segregation=false to create
                    // a fine/payment
                    var coll = collectorRepo.findByEmail("collector1@bookabin.local");
                    if (coll != null) {
                        bookingService.confirmBooking(saved.getId(), coll.getId());
                        bookingService.setSegregation(saved.getId(), false);
                    }
                }
            } catch (Exception ex) {
                // don't fail startup if smoke seed fails
            }
        };
    }
}
