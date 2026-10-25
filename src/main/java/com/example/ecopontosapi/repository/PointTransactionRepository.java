package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.PointTransaction;
import com.example.ecopontosapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Integer> {
    List<PointTransaction> findByUserOrderByCreatedAtDesc(User user);
}