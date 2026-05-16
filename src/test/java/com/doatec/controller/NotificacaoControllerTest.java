package com.doatec.controller;

import com.doatec.dto.response.NotificacaoResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.NotificacaoService;
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
@DisplayName("NotificacaoController")
class NotificacaoControllerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private NotificacaoController notificacaoController;

    private Pessoa usuario;
    private NotificacaoResponse notificacaoResponse;

    @BeforeEach
    void setUp() {
        usuario = new DoadorPF("123.456.789-00");
        usuario.setId(1);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("usuario@teste.com");
        usuario.setAtivo(true);

        notificacaoResponse = NotificacaoResponse.builder()
                .id(100)
                .titulo("Doacao aprovada")
                .mensagem("Sua doacao foi aprovada com sucesso.")
                .dataCriacao(LocalDateTime.now())
                .lida(false)
                .tipo("DOACAO")
                .entidadeRelacionadaId(10)
                .entidadeRelacionadaTipo("DOACAO")
                .build();
    }

    @Nested
    @DisplayName("listarNotificacoes endpoint")
    class ListarNotificacoesTests {

        @Test
        @DisplayName("GET retorna 200 com lista de notificacoes")
        void listarNotificacoesComSucesso() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
            when(notificacaoService.listarPorDestinatario(1)).thenReturn(List.of(notificacaoResponse));

            ResponseEntity<List<NotificacaoResponse>> responseEntity =
                    notificacaoController.listarNotificacoes(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(1, responseEntity.getBody().size());
            assertEquals("Doacao aprovada", responseEntity.getBody().get(0).titulo());

            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(notificacaoService).listarPorDestinatario(1);
        }

        @Test
        @DisplayName("GET retorna 200 com lista vazia quando nao ha notificacoes")
        void listarNotificacoesVazia() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
            when(notificacaoService.listarPorDestinatario(1)).thenReturn(List.of());

            ResponseEntity<List<NotificacaoResponse>> responseEntity =
                    notificacaoController.listarNotificacoes(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertTrue(responseEntity.getBody().isEmpty());
        }

        @Test
        @DisplayName("GET lanca excecao quando usuario nao encontrado")
        void listarNotificacoesUsuarioNaoEncontrado() {
            User userDetails = new User("inexistente@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> notificacaoController.listarNotificacoes(userDetails));
        }
    }

    @Nested
    @DisplayName("deletarNotificacao endpoint")
    class DeletarNotificacaoTests {

        @Test
        @DisplayName("DELETE com ownership retorna 200")
        void deletarComOwnership() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
            when(notificacaoService.deleteByIdAndDestinatario(100, 1)).thenReturn(true);

            ResponseEntity<Void> responseEntity =
                    notificacaoController.deletarNotificacao(100, userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            verify(notificacaoService).deleteByIdAndDestinatario(100, 1);
        }

        @Test
        @DisplayName("DELETE sem ownership retorna 403")
        void deletarSemOwnership() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));
            when(notificacaoService.deleteByIdAndDestinatario(100, 1)).thenReturn(false);

            ResponseEntity<Void> responseEntity =
                    notificacaoController.deletarNotificacao(100, userDetails);

            assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
            verify(notificacaoService).deleteByIdAndDestinatario(100, 1);
        }
    }
}
