package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, Integer> {
}