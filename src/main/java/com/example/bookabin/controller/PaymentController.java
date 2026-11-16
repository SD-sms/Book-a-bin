package com.example.bookabin.controller;

import com.example.bookabin.entity.Payment;
import com.example.bookabin.repository.PaymentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping("/user/payments/receipt/{id}")
    public String viewReceipt(@PathVariable Long id, Model model) {
        Optional<Payment> p = paymentRepository.findById(id);
        if (p.isEmpty()) {
            // Payment not found — redirect to payments list
            return "redirect:/user/payments";
        }
        model.addAttribute("payment", p.get());
        return "user/receipt";
    }
}
