package com.doatec.controller;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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

    @Autowired
    private PessoaRepository pessoaRepository;

    private Integer getAuthenticatedUserId(User userDetails) {
        return pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                .getId();
    }

    /**
     * Lista todas as notificações do usuário autenticado.
     */
    @GetMapping
    public ResponseEntity<List<NotificacaoResponse>> listarNotificacoes(
            @AuthenticationPrincipal User userDetails) {
        Integer userId = getAuthenticatedUserId(userDetails);
        List<NotificacaoResponse> notificacoes = notificacaoService.listarPorDestinatario(userId);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Lista apenas notificações não lidas do usuário autenticado.
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<List<NotificacaoResponse>> listarNaoLidas(
            @AuthenticationPrincipal User userDetails) {
        Integer userId = getAuthenticatedUserId(userDetails);
        List<NotificacaoResponse> notificacoes = notificacaoService.listarNaoLidas(userId);
        return ResponseEntity.ok(notificacoes);
    }

    /**
     * Conta notificações não lidas do usuário autenticado (para badge no frontend).
     */
    @GetMapping("/count")
    public ResponseEntity<Long> contarNaoLidas(@AuthenticationPrincipal User userDetails) {
        Long count = notificacaoService.contarNaoLidas(getAuthenticatedUserId(userDetails));
        return ResponseEntity.ok(count);
    }

    /**
     * Marca todas as notificações do usuário autenticado como lidas.
     */
    @PutMapping("/ler-todas")
    public ResponseEntity<Void> marcarComoLidas(@AuthenticationPrincipal User userDetails) {
        Integer userId = getAuthenticatedUserId(userDetails);
        notificacaoService.marcarTodasComoLidas(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Deleta uma notificação se pertencer ao usuário autenticado.
     */
    @DeleteMapping("/{notificacaoId}")
    public ResponseEntity<Void> deletarNotificacao(
            @PathVariable Integer notificacaoId,
            @AuthenticationPrincipal User userDetails) {
        Integer userId = getAuthenticatedUserId(userDetails);
        boolean deleted = notificacaoService.deleteByIdAndDestinatario(notificacaoId, userId);
        if (deleted) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
