package com.example.bookabin.service;

import com.example.bookabin.entity.User;
import com.example.bookabin.entity.Booking;
import com.example.bookabin.entity.Payment;
import com.example.bookabin.repository.UserRepository;
import com.example.bookabin.repository.BookingRepository;
import com.example.bookabin.repository.PaymentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BookingRepository bookingRepository,
                       PaymentRepository paymentRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // normalize role
        String role = user.getRole();
        if (role == null || role.isBlank()) role = "USER";
        role = role.trim();
        if (!role.startsWith("ROLE_")) role = "ROLE_" + role.toUpperCase();
        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Register a new user (resident)
     */
    public User registerUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        List<User> all = userRepository.findAll();
        return all.stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst();
    }

    /**
     * Find user by ID
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Update user profile (address, phone, etc.)
     */
    public User updateUserProfile(Long userId, User updatedUser) {
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPhone(updatedUser.getPhone());
            existingUser.setAddress(updatedUser.getAddress());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Get all bookings made by a user
     */
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    /**
     * Get payments for a user (adjust repository call as needed)
     */
    public Optional<Payment> getUserPayments(Long userId) {
        return paymentRepository.findById(userId);
    }

    /**
     * Change user password
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Delete a user account
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
