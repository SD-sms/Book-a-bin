package com.example.bookabin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fixed_fee")
public class FixedFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private double amount; // fee amount set by BBMP

    public FixedFee() {}

    public FixedFee(double amount) {
        this.amount = amount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}

