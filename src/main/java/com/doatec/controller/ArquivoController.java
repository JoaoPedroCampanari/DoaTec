package com.doatec.controller;

import com.doatec.service.ArquivoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Controller para acesso aos arquivos enviados.
 */
@RestController
public class ArquivoController {
    private final ArquivoService arquivos;
    private final Path diretorio;
    public ArquivoController(ArquivoService arquivos, @Value("${upload.path:uploads}") String caminho) {
        this.arquivos = arquivos;
        this.diretorio = Paths.get(caminho).toAbsolutePath().normalize();
    }
    /**
     * Retorna um arquivo ao usuário autorizado.
     */
    @GetMapping("/uploads/{nome:.+}")
    public ResponseEntity<Resource> ler(@PathVariable String nome, @AuthenticationPrincipal User usuario) {
        if (usuario == null) return ResponseEntity.status(401).build();
        if (!nome.matches("[a-fA-F0-9-]{36}\\.[a-zA-Z0-9]{1,10}")) return ResponseEntity.notFound().build();
        if (!arquivos.podeLer(nome, usuario.getUsername())) return ResponseEntity.notFound().build();
        Path caminho = diretorio.resolve(nome).normalize();
        if (!caminho.getParent().equals(diretorio) || !Files.isRegularFile(caminho, LinkOption.NOFOLLOW_LINKS)) return ResponseEntity.notFound().build();
        String extensao = nome.substring(nome.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        MediaType tipo = switch (extensao) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.parseMediaType("image/webp");
            case "pdf" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        ContentDisposition disposicao = tipo.equals(MediaType.APPLICATION_OCTET_STREAM)
                ? ContentDisposition.attachment().filename(nome).build()
                : ContentDisposition.inline().filename(nome).build();
        return ResponseEntity.ok().contentType(tipo)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store, private")
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(caminho));
    }
}
