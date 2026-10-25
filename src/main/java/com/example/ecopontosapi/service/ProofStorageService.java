package com.example.ecopontosapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// Guarda as fotos de comprovação em uma pasta do computador/servidor (padrão: "uploads").
@Service
public class ProofStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5 MB

    private final Path dir;

    public ProofStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) throws IOException {
        this.dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.dir);
    }

    // Salva a foto e devolve o nome do arquivo. Nunca usa o nome enviado pelo usuário.
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Envie uma foto como comprovação.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("A foto é muito grande (máximo 5 MB).");
        }
        try {
            byte[] head = new byte[12];
            int read;
            try (InputStream in = file.getInputStream()) {
                read = in.readNBytes(head, 0, head.length);
            }
            String ext = detectExtension(head, read);
            if (ext == null) {
                throw new IllegalArgumentException("Formato inválido. Envie uma foto JPG, PNG ou WEBP.");
            }
            String name = UUID.randomUUID() + "." + ext;
            Files.copy(file.getInputStream(), dir.resolve(name));
            return name;
        } catch (IOException e) {
            throw new IllegalArgumentException("Não foi possível salvar a foto. Tente novamente.");
        }
    }

    public Path resolve(String fileName) {
        Path p = dir.resolve(fileName).normalize();
        if (!p.startsWith(dir)) throw new IllegalArgumentException("Arquivo inválido.");
        return p;
    }

    public String contentType(String fileName) {
        String f = fileName.toLowerCase();
        if (f.endsWith(".png")) return "image/png";
        if (f.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    // Confere os primeiros bytes do arquivo (não confia só na extensão)
    private String detectExtension(byte[] h, int n) {
        if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        if (n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "webp";
        return null;
    }
}
