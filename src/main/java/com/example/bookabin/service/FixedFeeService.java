package com.example.bookabin.service;

import com.example.bookabin.entity.FixedFee;
import com.example.bookabin.repository.FixedFeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FixedFeeService {

    @Autowired
    private FixedFeeRepository fixedFeeRepository;

    /**
     * Get the latest fixed fee set by BBMP
     */
    public FixedFee getLatestFee() {
        return fixedFeeRepository.findTopByOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("No booking fee set yet"));
    }

    /**
     * Get all fixed fee records (history)
     */
    public List<FixedFee> getAllFees() {
        return fixedFeeRepository.findAll();
    }

    /**
     * Set a new booking fee
     */
    public FixedFee setNewFee(double amount) {
        FixedFee fee = new FixedFee();
        fee.setAmount(amount);
        return fixedFeeRepository.save(fee);
    }

    /**
     * Update an existing fee record
     */
    public FixedFee updateFee(Long id, double amount) {
        FixedFee fee = fixedFeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fee record not found"));
        fee.setAmount(amount);
        return fixedFeeRepository.save(fee);
    }

    /**
     * Delete a fee record
     */
    public void deleteFee(Long id) {
        fixedFeeRepository.deleteById(id);
    }
}
