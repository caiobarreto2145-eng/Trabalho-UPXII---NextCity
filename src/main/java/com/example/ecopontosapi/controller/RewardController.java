package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.Redemption;
import com.example.ecopontosapi.model.Reward;
import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.RewardRepository;
import com.example.ecopontosapi.service.RewardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RewardService rewardService;

    @GetMapping
    public List<Reward> listAll() {
        return rewardRepository.findAll();
    }

    @PostMapping("/{id}/redeem")
    public ResponseEntity<?> redeem(@PathVariable Integer id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            Redemption redemption = rewardService.redeem(user, id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Recompensa resgatada com sucesso!");
            response.put("reward", redemption.getReward().getName());
            response.put("pointsUsed", redemption.getPointsUsed());
            response.put("voucherCode", redemption.getVoucherCode());
            response.put("remainingPoints", user.getPoints());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}