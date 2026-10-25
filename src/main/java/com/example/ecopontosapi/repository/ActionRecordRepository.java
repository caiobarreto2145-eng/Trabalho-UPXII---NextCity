package com.example.ecopontosapi.repository;

import com.example.ecopontosapi.model.ActionRecord;
import com.example.ecopontosapi.model.SustainableAction;
import com.example.ecopontosapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ActionRecordRepository extends JpaRepository<ActionRecord, Integer> {
    List<ActionRecord> findByUserOrderByCreatedAtDesc(User user);

    // Limite diário: já existe um registro dessa ação hoje que não foi rejeitado?
    // (uma ação rejeitada não conta, então a pessoa pode tentar de novo)
    boolean existsByUserAndActionAndCreatedAtBetweenAndStatusNot(User user, SustainableAction action,
                                                                 LocalDateTime start, LocalDateTime end,
                                                                 String status);

    // Fila do administrador
    List<ActionRecord> findByStatusOrderByCreatedAtAsc(String status);
}
