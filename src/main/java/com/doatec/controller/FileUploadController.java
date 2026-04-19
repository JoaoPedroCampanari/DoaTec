package com.doatec.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private Path uploadDir;

    @PostConstruct
    public void init() {
        // Usa caminho absoluto baseado na raiz do projeto
        uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath().normalize();

        // Cria a pasta se não existir
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar a pasta de uploads: " + uploadDir, e);
        }
    }

    @PostMapping("/foto")
    public ResponseEntity<String> uploadFoto(@RequestParam("file") MultipartFile file) {
        // Valida tipo de arquivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Arquivo deve ser uma imagem.");
        }

        // Valida tamanho (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("Arquivo excede o tamanho máximo de 5MB.");
        }

        try {
            // Gera nome único para o arquivo
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Salva arquivo no caminho absoluto
            Path filePath = uploadDir.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            // Retorna URL relativa
            return ResponseEntity.ok("/uploads/" + uniqueFilename);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo: " + e.getMessage());
        }
    }
}