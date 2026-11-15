package com.doatec.controller;

import com.doatec.service.RedefinicaoSenhaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/redefinicao-senha")
public class RedefinicaoSenhaController {
    private final RedefinicaoSenhaService service;
    public RedefinicaoSenhaController(RedefinicaoSenhaService service) { this.service = service; }
    public record Solicitacao(@NotBlank @Email @Size(max = 120) String email) {}
    public record Confirmacao(@NotBlank @Pattern(regexp = "[0-9]{8}") String codigo,
                              @NotBlank @Size(min = 6, max = 20) String novaSenha) {}
    @PostMapping("/solicitar")
    public Map<String, String> solicitar(@Valid @RequestBody Solicitacao pedido) {
        service.solicitar(pedido.email());
        return Map.of("message", "Se houver uma conta com este e-mail, enviaremos um código.");
    }
    @PostMapping("/confirmar")
    public Map<String, String> confirmar(@Valid @RequestBody Confirmacao pedido) {
        service.confirmar(pedido.codigo(), pedido.novaSenha());
        return Map.of("message", "Senha redefinida com sucesso.");
    }
}
