package com.example.bookabin.repository;

import com.example.bookabin.entity.Payment;
import com.example.bookabin.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingUser(User user);
}
