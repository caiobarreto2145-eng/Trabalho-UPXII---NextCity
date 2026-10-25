package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.UserRepository;
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
@RequestMapping("/api/ranking")
public class RankingController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public Map<String, Object> getRanking(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        List<User> users = userRepository.findAllByOrderByTotalPointsDescIdAsc();

        List<Map<String, Object>> ranking = new ArrayList<>();
        Integer yourPosition = null;

        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            int position = i + 1;

            Map<String, Object> entry = new HashMap<>();
            entry.put("position", position);
            entry.put("name", u.getName());
            entry.put("points", u.getTotalPoints()); // pontos acumulados
            entry.put("level", calculateLevel(u.getTotalPoints()));
            ranking.add(entry);

            if (u.getId().equals(currentUser.getId())) {
                yourPosition = position;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("ranking", ranking);
        response.put("yourPosition", yourPosition);
        return response;
    }

    private String calculateLevel(Integer points) {
        if (points >= 600) return "Embaixador Verde";
        if (points >= 300) return "Guardião Verde";
        if (points >= 100) return "Explorador Verde";
        return "Iniciante";
    }
}