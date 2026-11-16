package com.example.bookabin.service;

import com.example.bookabin.entity.Booking;
import com.example.bookabin.entity.Payment;
import com.example.bookabin.entity.User;
import com.example.bookabin.repository.BookingRepository;
import com.example.bookabin.repository.PaymentRepository;
import com.example.bookabin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @SuppressWarnings("unused")
	@Autowired
    private UserRepository userRepository;

    /**
     * Get all payments for a user
     */
    public List<Payment> getPaymentsByUser(User user) {
        return paymentRepository.findByBookingUser(user);
    }

    /**
     * Make a new payment for a booking (includes fine if any)
     */
    public Payment makePayment(Long bookingId, String paymentMethod, String transactionId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        User user = booking.getUser();

        // Base amount = booking fee
        double baseAmount = booking.getAmount();

        // Add fine if applicable
        double fineAmount = booking.getFine() != null ? booking.getFine() : 0.0;

        double totalAmount = baseAmount + fineAmount;

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setBooking(booking);
        payment.setAmount(totalAmount);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionId(transactionId);

        // Save payment
        paymentRepository.save(payment);

        // Update booking status to "Paid"
        booking.setStatus("Paid");
        bookingRepository.save(booking);

        return payment;
    }
}
