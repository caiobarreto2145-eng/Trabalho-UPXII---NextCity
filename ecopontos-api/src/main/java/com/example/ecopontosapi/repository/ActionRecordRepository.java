package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.ActionRecord;
import com.example.ecopontosapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRecordRepository extends JpaRepository<ActionRecord, Integer> {
    List<ActionRecord> findByUserOrderByCreatedAtDesc(User user);
}