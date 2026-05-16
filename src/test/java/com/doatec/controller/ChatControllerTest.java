package com.doatec.controller;

import com.doatec.dto.request.ChatRequest;
import com.doatec.dto.response.ChatResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.chat.ContextoChat;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController")
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private ChatController chatController;

    private Pessoa remetente;

    @BeforeEach
    void setUp() {
        remetente = new DoadorPF("123.456.789-00");
        remetente.setId(1);
        remetente.setNome("Usuario Teste");
        remetente.setEmail("usuario@teste.com");
        remetente.setAtivo(true);
    }

    @Nested
    @DisplayName("enviarMensagem endpoint")
    class EnviarMensagemTests {

        @Test
        @DisplayName("POST /api/chat/enviar retorna 201 com mensagem criada")
        void enviarMensagemComSucesso() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            ChatRequest request = new ChatRequest("Olá, preciso de ajuda.", 1, ContextoChat.SUPORTE);
            ChatResponse responseDto = new ChatResponse(
                    1,
                    "Olá, preciso de ajuda.",
                    LocalDateTime.of(2026, 5, 16, 10, 30),
                    1,
                    "Usuario Teste",
                    "USER"
            );

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(remetente));
            when(chatService.enviarMensagem(request, remetente)).thenReturn(responseDto);

            ResponseEntity<ChatResponse> response = chatController.enviarMensagem(request, userDetails);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());
            assertEquals("Olá, preciso de ajuda.", response.getBody().conteudo());
            assertEquals(1, response.getBody().remetenteId());
            assertEquals("Usuario Teste", response.getBody().remetenteNome());
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(chatService).enviarMensagem(request, remetente);
        }

        @Test
        @DisplayName("POST /api/chat/enviar lanca excecao quando usuario nao encontrado")
        void enviarMensagemUsuarioNaoEncontrado() {
            User userDetails = new User("inexistente@teste.com", "", List.of());
            ChatRequest request = new ChatRequest("Mensagem", 1, ContextoChat.SUPORTE);

            when(pessoaRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> chatController.enviarMensagem(request, userDetails));
            verify(pessoaRepository).findByEmail("inexistente@teste.com");
            verify(chatService, never()).enviarMensagem(any(), any());
        }

        @Test
        @DisplayName("POST /api/chat/enviar lanca excecao quando acesso negado pelo servico")
        void enviarMensagemAcessoNegado() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            ChatRequest request = new ChatRequest("Mensagem", 99, ContextoChat.SUPORTE);

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(remetente));
            when(chatService.enviarMensagem(request, remetente))
                    .thenThrow(new RuntimeException("Você não tem acesso a este chat"));

            assertThrows(RuntimeException.class, () -> chatController.enviarMensagem(request, userDetails));
            verify(chatService).enviarMensagem(request, remetente);
        }
    }

    @Nested
    @DisplayName("buscarHistorico endpoint")
    class BuscarHistoricoTests {

        @Test
        @DisplayName("GET /api/chat/historico/{contexto}/{referenciaId} retorna 200 com historico")
        void buscarHistoricoComSucesso() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            ChatResponse msg1 = new ChatResponse(
                    1, "Primeira mensagem",
                    LocalDateTime.of(2026, 5, 16, 10, 0),
                    1, "Usuario Teste", "USER"
            );
            ChatResponse msg2 = new ChatResponse(
                    2, "Resposta do admin",
                    LocalDateTime.of(2026, 5, 16, 10, 5),
                    2, "Admin Teste", "ADMIN"
            );

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(remetente));
            when(chatService.buscarHistorico(ContextoChat.SUPORTE, 1, remetente))
                    .thenReturn(List.of(msg1, msg2));

            ResponseEntity<List<ChatResponse>> response = chatController.buscarHistorico(
                    ContextoChat.SUPORTE, 1, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals("Primeira mensagem", response.getBody().get(0).conteudo());
            assertEquals("Resposta do admin", response.getBody().get(1).conteudo());
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(chatService).buscarHistorico(ContextoChat.SUPORTE, 1, remetente);
        }

        @Test
        @DisplayName("GET /api/chat/historico/{contexto}/{referenciaId} retorna 200 com lista vazia")
        void buscarHistoricoVazio() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(remetente));
            when(chatService.buscarHistorico(ContextoChat.DOACAO, 5, remetente))
                    .thenReturn(List.of());

            ResponseEntity<List<ChatResponse>> response = chatController.buscarHistorico(
                    ContextoChat.DOACAO, 5, userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isEmpty());
            verify(chatService).buscarHistorico(ContextoChat.DOACAO, 5, remetente);
        }

        @Test
        @DisplayName("GET /api/chat/historico lanca excecao quando usuario nao encontrado")
        void buscarHistoricoUsuarioNaoEncontrado() {
            User userDetails = new User("inexistente@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                    chatController.buscarHistorico(ContextoChat.SUPORTE, 1, userDetails));
            verify(pessoaRepository).findByEmail("inexistente@teste.com");
            verify(chatService, never()).buscarHistorico(any(), any(), any());
        }

        @Test
        @DisplayName("GET /api/chat/historico lanca excecao quando acesso negado pelo servico")
        void buscarHistoricoAcessoNegado() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(remetente));
            when(chatService.buscarHistorico(ContextoChat.SUPORTE, 99, remetente))
                    .thenThrow(new RuntimeException("Você não tem acesso a este chat"));

            assertThrows(RuntimeException.class, () ->
                    chatController.buscarHistorico(ContextoChat.SUPORTE, 99, userDetails));
            verify(chatService).buscarHistorico(ContextoChat.SUPORTE, 99, remetente);
        }
    }
}
