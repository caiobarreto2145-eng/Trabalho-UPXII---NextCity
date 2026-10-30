package com.example.ecopontosapi.service;

import com.example.ecopontosapi.model.*;
import com.example.ecopontosapi.repository.ActionRecordRepository;
import com.example.ecopontosapi.repository.PointTransactionRepository;
import com.example.ecopontosapi.repository.SustainableActionRepository;
import com.example.ecopontosapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
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

    @Autowired
    private ProofStorageService proofStorageService;

    // Registra a ação. Se ela exige comprovação, fica PENDING (sem pontos) até um admin aprovar.
    @Transactional
    public ActionRecord registerAction(User user, Integer actionId, MultipartFile photo) {

        SustainableAction action = actionRepository.findById(actionId)
                .orElseThrow(() -> new IllegalArgumentException("Ação não encontrada"));

        if (!action.getActive()) {
            throw new IllegalArgumentException("Essa ação não está disponível no momento");
        }

        // Limite diário: uma vez por dia por ação (ações rejeitadas não contam)
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        if (actionRecordRepository.existsByUserAndActionAndCreatedAtBetweenAndStatusNot(
                user, action, startOfDay, endOfDay, "REJECTED")) {
            throw new IllegalArgumentException("Você já registrou essa ação hoje. Volte amanhã!");
        }

        boolean needsProof = Boolean.TRUE.equals(action.getRequiresProof());

        ActionRecord record = new ActionRecord();
        record.setUser(user);
        record.setAction(action);
        record.setPointsEarned(action.getPoints());
        record.setCreatedAt(LocalDateTime.now());

        if (needsProof) {
            // Exige foto; os pontos só entram quando o administrador aprovar
            record.setPhotoPath(proofStorageService.store(photo));
            record.setStatus("PENDING");
            return actionRecordRepository.save(record);
        }

        record.setStatus("APPROVED");
        actionRecordRepository.save(record);
        creditPoints(record);
        return record;
    }

    @Transactional
    public ActionRecord approve(Integer recordId) {
        ActionRecord record = actionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado"));
        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalArgumentException("Esse registro já foi analisado.");
        }
        record.setStatus("APPROVED");
        record.setReviewedAt(LocalDateTime.now());
        actionRecordRepository.save(record);
        creditPoints(record);
        return record;
    }

    @Transactional
    public ActionRecord reject(Integer recordId, String note) {
        ActionRecord record = actionRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Registro não encontrado"));
        if (!"PENDING".equals(record.getStatus())) {
            throw new IllegalArgumentException("Esse registro já foi analisado.");
        }
        record.setStatus("REJECTED");
        record.setReviewedAt(LocalDateTime.now());
        if (note != null && !note.isBlank()) {
            record.setReviewNote(note.trim().length() > 250 ? note.trim().substring(0, 250) : note.trim());
        }
        return actionRecordRepository.save(record);
    }

    // Soma os pontos (saldo + acumulado do ranking) e registra no extrato
    private void creditPoints(ActionRecord record) {
        User owner = record.getUser();
        int pts = record.getPointsEarned();
        owner.setPoints(owner.getPoints() + pts);
        owner.setTotalPoints(owner.getTotalPoints() + pts);
        userRepository.save(owner);

        PointTransaction transaction = new PointTransaction();
        transaction.setUser(owner);
        transaction.setType("EARN");
        transaction.setAmount(pts);
        transaction.setDescription("Ação registrada: " + record.getAction().getName());
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(transaction);
    }
}
