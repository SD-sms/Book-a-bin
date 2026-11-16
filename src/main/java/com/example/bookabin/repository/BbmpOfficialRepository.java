package com.example.bookabin.repository;

import com.example.bookabin.entity.BbmpOfficial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BbmpOfficialRepository extends JpaRepository<BbmpOfficial, Long> {
    Optional<BbmpOfficial> findByEmail(String email);

    Optional<BbmpOfficial> findByName(String name);
}
