package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.ActionRecord;
import com.example.ecopontosapi.model.SustainableAction;
import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.SustainableActionRepository;
import com.example.ecopontosapi.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/{id}/register")
    public ResponseEntity<?> registerAction(@PathVariable Integer id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        try {
            ActionRecord record = actionService.registerAction(user, id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ação registrada! Você ganhou +" + record.getPointsEarned() + " pontos.");
            response.put("totalPoints", user.getPoints());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}