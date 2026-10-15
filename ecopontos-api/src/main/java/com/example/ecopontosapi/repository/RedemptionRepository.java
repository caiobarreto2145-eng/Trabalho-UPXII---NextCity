package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.Redemption;
import com.example.ecopontosapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RedemptionRepository extends JpaRepository<Redemption, Integer> {
    List<Redemption> findByUserOrderByCreatedAtDesc(User user);
}