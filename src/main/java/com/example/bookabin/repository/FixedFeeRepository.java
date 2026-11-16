package com.example.bookabin.repository;

import java.util.Optional;

import com.example.bookabin.entity.FixedFee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixedFeeRepository extends JpaRepository<FixedFee, Long> {

    Optional<FixedFee> findTopByOrderByIdDesc();

}
