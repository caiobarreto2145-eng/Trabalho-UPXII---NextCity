package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.ActionRecord;
import com.example.ecopontosapi.model.SustainableAction;
import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.SustainableActionRepository;
import com.example.ecopontosapi.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    @Autowired
    private SustainableActionRepository actionRepository;

    @Autowired
    private ActionService actionService;

    @GetMapping
    public List<SustainableAction> listAll() {
        return actionRepository.findAll();
    }

    // A foto é opcional na chamada: ações que exigem comprovação rejeitam o pedido sem ela.
    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerAction(@PathVariable Integer id,
                                            @RequestParam(value = "photo", required = false) MultipartFile photo,
                                            Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            ActionRecord record = actionService.registerAction(user, id, photo);
            Map<String, Object> response = new HashMap<>();
            if ("PENDING".equals(record.getStatus())) {
                response.put("message", "Foto enviada! Os pontos entram assim que um administrador aprovar.");
            } else {
                response.put("message", "Ação registrada! Você ganhou +" + record.getPointsEarned() + " pontos.");
            }
            response.put("status", record.getStatus());
            response.put("totalPoints", user.getPoints());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
