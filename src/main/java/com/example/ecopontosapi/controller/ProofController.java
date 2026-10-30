package com.example.ecopontosapi.controller;

import com.example.ecopontosapi.model.ActionRecord;
import com.example.ecopontosapi.model.User;
import com.example.ecopontosapi.repository.ActionRecordRepository;
import com.example.ecopontosapi.service.ActionService;
import com.example.ecopontosapi.service.ProofStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProofController {

    @Autowired
    private ActionRecordRepository actionRecordRepository;

    @Autowired
    private ActionService actionService;

    @Autowired
    private ProofStorageService proofStorageService;

    private boolean isAdmin(User user) {
        return "ADMIN".equals(user.getRole());
    }

    private ResponseEntity<?> forbidden() {
        return ResponseEntity.status(403).body(Map.of("error", "Apenas administradores podem fazer isso."));
    }

    // Lista para o administrador (padrão: pendentes)
    @GetMapping("/admin/proofs")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "PENDING") String status, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!isAdmin(user)) return forbidden();

        List<Map<String, Object>> list = new ArrayList<>();
        for (ActionRecord r : actionRecordRepository.findByStatusOrderByCreatedAtAsc(status.toUpperCase())) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("userName", r.getUser().getName());
            m.put("actionName", r.getAction().getName());
            m.put("points", r.getPointsEarned());
            m.put("status", r.getStatus());
            m.put("createdAt", r.getCreatedAt());
            m.put("hasPhoto", r.getPhotoPath() != null);
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }

    @PostMapping("/admin/proofs/{id}/approve")
    public ResponseEntity<?> approve(@PathVariable Integer id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!isAdmin(user)) return forbidden();
        try {
            actionService.approve(id);
            return ResponseEntity.ok(Map.of("message", "Ação aprovada e pontos creditados."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/admin/proofs/{id}/reject")
    public ResponseEntity<?> reject(@PathVariable Integer id,
                                    @RequestBody(required = false) Map<String, String> body,
                                    Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        if (!isAdmin(user)) return forbidden();
        try {
            actionService.reject(id, body == null ? null : body.get("note"));
            return ResponseEntity.ok(Map.of("message", "Ação rejeitada."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Foto: só o dono da ação ou um administrador pode ver
    @GetMapping("/proofs/{id}/photo")
    public ResponseEntity<?> photo(@PathVariable Integer id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ActionRecord r = actionRecordRepository.findById(id).orElse(null);
        if (r == null || r.getPhotoPath() == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Foto não encontrada."));
        }
        if (!isAdmin(user) && !r.getUser().getId().equals(user.getId())) return forbidden();

        try {
            var path = proofStorageService.resolve(r.getPhotoPath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body(Map.of("error", "Arquivo da foto não encontrado."));
            }
            Resource file = new FileSystemResource(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(proofStorageService.contentType(r.getPhotoPath())))
                    .body(file);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
