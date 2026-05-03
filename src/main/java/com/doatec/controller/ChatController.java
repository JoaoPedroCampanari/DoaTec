package com.doatec.controller;

import com.doatec.dto.request.ChatRequest;
import com.doatec.dto.response.ChatResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.chat.ContextoChat;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private PessoaRepository pessoaRepository;

    @PostMapping("/enviar")
    public ResponseEntity<ChatResponse> enviarMensagem(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal User userDetails) {
        Pessoa remetente = getAuthenticatedUser(userDetails);
        ChatResponse response = chatService.enviarMensagem(request, remetente);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/historico/{contexto}/{referenciaId}")
    public ResponseEntity<List<ChatResponse>> buscarHistorico(
            @PathVariable ContextoChat contexto,
            @PathVariable Integer referenciaId,
            @AuthenticationPrincipal User userDetails) {
        Pessoa usuario = getAuthenticatedUser(userDetails);
        List<ChatResponse> historico = chatService.buscarHistorico(contexto, referenciaId, usuario);
        return ResponseEntity.ok(historico);
    }

    private Pessoa getAuthenticatedUser(User userDetails) {
        return pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}
