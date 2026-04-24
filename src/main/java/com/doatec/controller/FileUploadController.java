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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    private Path uploadDir;

    /** Magic bytes para validação de imagem real (previne content-type spoofing) */
    private static final Map<String, byte[]> IMAGE_SIGNATURES = Map.of(
        "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        "image/png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        "image/gif",  new byte[]{0x47, 0x49, 0x46},
        "image/webp", new byte[]{0x52, 0x49, 0x46, 0x46}  // RIFF header (WebP container)
    );

    @PostConstruct
    public void init() {
        uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath().normalize();

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
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body("Arquivo deve ser uma imagem.");
        }

        // Valida magic bytes contra spoofing de content-type
        if (!validateImageSignature(file, contentType)) {
            return ResponseEntity.badRequest().body("Assinatura do arquivo não corresponde ao tipo declarado.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("Arquivo excede o tamanho máximo de 5MB.");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadDir.resolve(uniqueFilename);
            file.transferTo(filePath.toFile());

            return ResponseEntity.ok("/uploads/" + uniqueFilename);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Erro ao salvar arquivo: " + e.getMessage());
        }
    }

    private boolean validateImageSignature(MultipartFile file, String contentType) {
        byte[] expected = IMAGE_SIGNATURES.get(contentType);
        if (expected == null) {
            // Tipo de imagem não mapeado — permite mas loga warning
            return true;
        }

        try {
            byte[] header = new byte[expected.length];
            int bytesRead = file.getInputStream().read(header);
            if (bytesRead < expected.length) return false;

            for (int i = 0; i < expected.length; i++) {
                if (header[i] != expected[i]) return false;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
