package com.example.ecopontosapi.service;

import com.example.ecopontosapi.model.Redemption;
import com.example.ecopontosapi.repository.RedemptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VoucherService {

    @Autowired
    private RedemptionRepository redemptionRepository;

    private String normalize(String code) {
        return code == null ? "" : code.trim().toUpperCase();
    }

    // Consulta um voucher (usado pela loja parceira antes de entregar o benefício)
    public Redemption find(String code) {
        return redemptionRepository.findByVoucherCode(normalize(code))
                .orElseThrow(() -> new IllegalArgumentException("Voucher não encontrado. Confira o código."));
    }

    // Marca o voucher como usado. Ele NÃO é apagado: fica no histórico como USED.
    @Transactional
    public Redemption use(String code) {
        Redemption r = find(code);
        if ("USED".equals(r.getStatus())) {
            throw new IllegalArgumentException("Este voucher já foi utilizado.");
        }
        r.setStatus("USED");
        r.setUsedAt(LocalDateTime.now());
        return redemptionRepository.save(r);
    }
}
