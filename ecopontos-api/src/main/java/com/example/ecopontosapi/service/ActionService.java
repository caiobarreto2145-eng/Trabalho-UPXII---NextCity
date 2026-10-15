package com.example.ecopontosapi.service;

import com.example.ecopontosapi.model.*;
import com.example.ecopontosapi.repository.ActionRecordRepository;
import com.example.ecopontosapi.repository.PointTransactionRepository;
import com.example.ecopontosapi.repository.SustainableActionRepository;
import com.example.ecopontosapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ActionService {

    @Autowired
    private SustainableActionRepository actionRepository;

    @Autowired
    private ActionRecordRepository actionRecordRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ActionRecord registerAction(User user, Integer actionId) {

        SustainableAction action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada"));

        if (!action.getActive()) {
            throw new IllegalArgumentException("Essa ação não está disponível no momento");
        }

        // 1. Cria o registro da ação realizada
        ActionRecord record = new ActionRecord();
        record.setUser(user);
        record.setAction(action);
        record.setPointsEarned(action.getPoints());
        record.setCreatedAt(LocalDateTime.now());
        actionRecordRepository.save(record);

        // 2. Soma os pontos no usuário
        user.setPoints(user.getPoints() + action.getPoints());
        userRepository.save(user);

        // 3. Registra a movimentação no extrato de pontos
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(user);
        transaction.setType("EARN");
        transaction.setAmount(action.getPoints());
        transaction.setDescription("Ação registrada: " + action.getName());
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(transaction);

        return record;
    }
}