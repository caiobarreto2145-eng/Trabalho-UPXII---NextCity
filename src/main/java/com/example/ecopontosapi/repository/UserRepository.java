package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    List<User> findAllByOrderByPointsDesc();
    // Ranking: mais pontos acumulados primeiro; em caso de empate, quem entrou antes (id menor)
    List<User> findAllByOrderByTotalPointsDescIdAsc();
}