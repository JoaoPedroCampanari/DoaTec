package com.doatec.controller;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento de notificações dos usuários.
 */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    /**
     * Lista todas as notificações de um usuário.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacaoResponse>> listarNotificacoes(
            @PathVariable Integer usuarioId) {
        List<NotificacaoResponse> notificacoes = notificacaoService.listarPorDestinatario(usuarioId);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Lista apenas notificações não lidas de um usuário.
     */
    @GetMapping("/usuario/{usuarioId}/nao-lidas")
    public ResponseEntity<List<NotificacaoResponse>> listarNaoLidas(
            @PathVariable Integer usuarioId) {
        List<NotificacaoResponse> notificacoes = notificacaoService.listarNaoLidas(usuarioId);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Conta notificações não lidas (para badge no frontend).
     */
    @GetMapping("/usuario/{usuarioId}/count")
    public ResponseEntity<Long> contarNaoLidas(@PathVariable Integer usuarioId) {
        Long count = notificacaoService.contarNaoLidas(usuarioId);
        return ResponseEntity.ok(count);
    }

    /**
     * Retorna resumo de notificações (count + últimas 5).
     */
    @GetMapping("/usuario/{usuarioId}/resumo")
    public ResponseEntity<NotificacaoSummaryResponse> resumoNotificacoes(
            @PathVariable Integer usuarioId) {
        Long totalNaoLidas = notificacaoService.contarNaoLidas(usuarioId);
        List<NotificacaoResponse> ultimas = notificacaoService.listarUltimas(usuarioId, 5);

        NotificacaoSummaryResponse summary = NotificacaoSummaryResponse.builder()
                .totalNaoLidas(totalNaoLidas)
                .ultimasNotificacoes(ultimas)
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Marca uma notificação como lida.
     */
    @PutMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Integer id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Marca todas as notificações de um usuário como lidas.
     */
    @PutMapping("/usuario/{usuarioId}/ler-todas")
    public ResponseEntity<Void> marcarTodasComoLidas(@PathVariable Integer usuarioId) {
        notificacaoService.marcarTodasComoLidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    /**
     * DTO para resumo de notificações.
     */
    @lombok.Builder
    @lombok.Data
    public static class NotificacaoSummaryResponse {
        private Long totalNaoLidas;
        private List<NotificacaoResponse> ultimasNotificacoes;
    }
}