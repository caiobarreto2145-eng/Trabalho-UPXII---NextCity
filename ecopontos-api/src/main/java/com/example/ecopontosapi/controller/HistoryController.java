package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.*;
import com.example.ecopontosapi.repository.ActionRecordRepository;
import com.example.ecopontosapi.repository.PointTransactionRepository;
import com.example.ecopontosapi.repository.RedemptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private ActionRecordRepository actionRecordRepository;

    @Autowired
    private RedemptionRepository redemptionRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @GetMapping
    public Map<String, Object> getHistory(Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        List<Map<String, Object>> actions = new ArrayList<>();
        for (ActionRecord record : actionRecordRepository.findByUserOrderByCreatedAtDesc(user)) {
            Map<String, Object> item = new HashMap<>();
            item.put("actionName", record.getAction().getName());
            item.put("pointsEarned", record.getPointsEarned());
            item.put("date", record.getCreatedAt());
            actions.add(item);
        }

        List<Map<String, Object>> redemptions = new ArrayList<>();
        for (Redemption redemption : redemptionRepository.findByUserOrderByCreatedAtDesc(user)) {
            Map<String, Object> item = new HashMap<>();
            item.put("rewardName", redemption.getReward().getName());
            item.put("pointsUsed", redemption.getPointsUsed());
            item.put("date", redemption.getCreatedAt());
            redemptions.add(item);
        }

        List<Map<String, Object>> transactions = new ArrayList<>();
        for (PointTransaction transaction : pointTransactionRepository.findByUserOrderByCreatedAtDesc(user)) {
            Map<String, Object> item = new HashMap<>();
            item.put("type", transaction.getType());
            item.put("description", transaction.getDescription());
            item.put("amount", transaction.getAmount());
            item.put("date", transaction.getCreatedAt());
            transactions.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("actions", actions);
        response.put("redemptions", redemptions);
        response.put("transactions", transactions);
        return response;
    }
}