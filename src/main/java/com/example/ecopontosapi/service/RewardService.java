package com.example.ecopontosapi.service;

import com.example.ecopontosapi.model.*;
import com.example.ecopontosapi.repository.PointTransactionRepository;
import com.example.ecopontosapi.repository.RedemptionRepository;
import com.example.ecopontosapi.repository.RewardRepository;
import com.example.ecopontosapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class RewardService {

    @Autowired
    private RewardRepository rewardRepository;

    @Autowired
    private RedemptionRepository redemptionRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Sem letras/números parecidos (0/O, 1/I) para ninguém errar ao digitar o código na loja
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    private String block(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        return sb.toString();
    }

    // Gera códigos no formato ECO-7F3K-92QX e garante que ainda não existe
    private String generateVoucherCode() {
        String code;
        do {
            code = "ECO-" + block(4) + "-" + block(4);
        } while (redemptionRepository.existsByVoucherCode(code));
        return code;
    }

    @Transactional
    public Redemption redeem(User user, Integer rewardId) {

        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new IllegalArgumentException("Recompensa não encontrada"));

        if (!reward.getActive()) {
            throw new IllegalArgumentException("Essa recompensa não está disponível no momento");
        }

        if (reward.getStock() <= 0) {
            throw new IllegalArgumentException("Essa recompensa está fora de estoque");
        }

        if (user.getPoints() < reward.getPointsRequired()) {
            throw new IllegalArgumentException("Você ainda não possui pontos suficientes para essa recompensa");
        }

        // 1. Desconta os pontos do usuário
        user.setPoints(user.getPoints() - reward.getPointsRequired());
        userRepository.save(user);

        // 2. Reduz o estoque da recompensa
        reward.setStock(reward.getStock() - 1);
        rewardRepository.save(reward);

        // 3. Registra o resgate
        Redemption redemption = new Redemption();
        redemption.setUser(user);
        redemption.setReward(reward);
        redemption.setPointsUsed(reward.getPointsRequired());
        redemption.setCreatedAt(LocalDateTime.now());
        redemption.setVoucherCode(generateVoucherCode());
        redemption.setStatus("ACTIVE");
        redemptionRepository.save(redemption);

        // 4. Registra a movimentação no extrato de pontos
        PointTransaction transaction = new PointTransaction();
        transaction.setUser(user);
        transaction.setType("REDEEM");
        transaction.setAmount(reward.getPointsRequired());
        transaction.setDescription("Resgate: " + reward.getName());
        transaction.setCreatedAt(LocalDateTime.now());
        pointTransactionRepository.save(transaction);

        return redemption;
    }
}