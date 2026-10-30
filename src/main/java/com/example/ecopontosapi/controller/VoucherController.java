package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.Redemption;
import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.RedemptionRepository;
import com.example.ecopontosapi.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherController {

    @Autowired
    private RedemptionRepository redemptionRepository;

    @Autowired
    private VoucherService voucherService;

    private Map<String, Object> toMap(Redemption r, boolean withOwner) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", r.getVoucherCode());
        m.put("rewardName", r.getReward().getName());
        m.put("pointsUsed", r.getPointsUsed());
        m.put("status", r.getStatus());
        m.put("createdAt", r.getCreatedAt());
        m.put("usedAt", r.getUsedAt());
        if (withOwner) m.put("userName", r.getUser().getName());
        return m;
    }

    // Só PARTNER (loja) e ADMIN podem consultar/validar o voucher de outras pessoas
    private boolean canValidate(User user) {
        return "PARTNER".equals(user.getRole()) || "ADMIN".equals(user.getRole());
    }

    // Meus vouchers
    @GetMapping
    public List<Map<String, Object>> mine(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Redemption r : redemptionRepository.findByUserOrderByCreatedAtDesc(user)) {
            list.add(toMap(r, false));
        }
        return list;
    }

    // Consultar um voucher pelo código (tela de validação da loja)
    @GetMapping("/{code}")
    public ResponseEntity<?> lookup(@PathVariable String code, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!canValidate(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Apenas lojas parceiras podem validar vouchers."));
        }
        try {
            return ResponseEntity.ok(toMap(voucherService.find(code), true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    // Marcar como usado
    @PostMapping("/{code}/use")
    public ResponseEntity<?> use(@PathVariable String code, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!canValidate(user)) {
            return ResponseEntity.status(403).body(Map.of("error", "Apenas lojas parceiras podem validar vouchers."));
        }
        try {
            return ResponseEntity.ok(toMap(voucherService.use(code), true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
