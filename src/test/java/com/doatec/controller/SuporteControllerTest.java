package com.doatec.controller;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.SuporteFormularioService;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuporteController - Meus Tickets")
class SuporteControllerTest {

    @Mock
    private SuporteFormularioService suporteService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private SuporteController suporteController;

    private Pessoa autor;

    @BeforeEach
    void setUp() {
        autor = new DoadorPF("123.456.789-00");
        autor.setId(1);
        autor.setNome("Usuario Teste");
        autor.setEmail("usuario@teste.com");
        autor.setAtivo(true);
    }

    @Nested
    @DisplayName("receberFormulario endpoint")
    class ReceberFormularioTests {

        @Test
        @DisplayName("POST /api/suporte retorna 201 com ticket criado")
        void criarTicketComSucesso() {
            SuporteFormularioRequest request = SuporteFormularioRequest.builder()
                    .nome("Usuario Teste")
                    .email("usuario@teste.com")
                    .assunto("Problema com hardware")
                    .mensagem("Meu notebook nao liga.")
                    .build();

            SuporteFormulario ticket = SuporteFormulario.builder()
                    .id(1)
                    .autor(autor)
                    .assunto("Problema com hardware")
                    .mensagem("Meu notebook nao liga.")
                    .status(StatusSuporte.ABERTO)
                    .build();

            when(suporteService.criarTicket(request)).thenReturn(ticket);

            ResponseEntity<SuporteResponse> response = suporteController.receberFormulario(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().id());
            assertEquals("Problema com hardware", response.getBody().assunto());
            assertEquals("Meu notebook nao liga.", response.getBody().mensagem());
            assertEquals("ABERTO", response.getBody().status());
            verify(suporteService).criarTicket(request);
        }

        @Test
        @DisplayName("POST /api/suporte lanca excecao quando email nao encontrado")
        void lancaExcecaoQuandoEmailNaoEncontrado() {
            SuporteFormularioRequest request = SuporteFormularioRequest.builder()
                    .nome("Usuario Teste")
                    .email("inexistente@teste.com")
                    .assunto("Problema")
                    .mensagem("Mensagem teste")
                    .build();

            when(suporteService.criarTicket(request))
                    .thenThrow(new RuntimeException("Nenhuma conta encontrada com o email fornecido."));

            assertThrows(RuntimeException.class, () -> suporteController.receberFormulario(request));
            verify(suporteService).criarTicket(request);
        }
    }

    @Nested
    @DisplayName("excluirTicket endpoint")
    class ExcluirTicketTests {

        @Test
        @DisplayName("DELETE /api/suporte/{id} retorna 204 quando autor e dono do ticket")
        void excluirTicketComSucesso() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            doNothing().when(suporteService).excluirTicket(1, 1);

            ResponseEntity<Void> response = suporteController.excluirTicket(1, userDetails);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(suporteService).excluirTicket(1, 1);
        }

        @Test
        @DisplayName("DELETE /api/suporte/{id} retorna 401 quando userDetails e nulo")
        void excluirTicketSemAutenticacao() {
            ResponseEntity<Void> response = suporteController.excluirTicket(1, null);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verify(pessoaRepository, never()).findByEmail(any());
            verify(suporteService, never()).excluirTicket(any(), any());
        }

        @Test
        @DisplayName("DELETE /api/suporte/{id} retorna 401 quando usuario nao existe no banco")
        void excluirTicketUsuarioNaoExiste() {
            User userDetails = new User("inexistente@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            ResponseEntity<Void> response = suporteController.excluirTicket(1, userDetails);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verify(pessoaRepository).findByEmail("inexistente@teste.com");
            verify(suporteService, never()).excluirTicket(any(), any());
        }

        @Test
        @DisplayName("DELETE /api/suporte/{id} lanca excecao quando nao e o dono do ticket")
        void excluirTicketSemPermissao() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            doThrow(new RuntimeException("Sem permissão para excluir este ticket"))
                    .when(suporteService).excluirTicket(99, 1);

            assertThrows(RuntimeException.class, () -> suporteController.excluirTicket(99, userDetails));
            verify(suporteService).excluirTicket(99, 1);
        }
    }

    @Nested
    @DisplayName("getMeusTickets endpoint")
    class GetMeusTicketsTests {

        @Test
        @DisplayName("Retorna lista de tickets do autor autenticado")
        void retornaTicketsDoAutor() {
            User userDetails = new User("usuario@teste.com", "", List.of());

            SuporteResponse responseDto = SuporteResponse.builder()
                    .id(1)
                    .autorNome("Usuario Teste")
                    .autorEmail("usuario@teste.com")
                    .assunto("Hardware solicitado")
                    .mensagem("Preciso de um notebook.")
                    .status("ABERTO")
                    .resposta("Seu pedido foi aprovado.")
                    .build();

            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            when(suporteService.getTicketsByAutorId(1)).thenReturn(List.of(responseDto));

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertEquals(1, responseEntity.getBody().size());
            assertEquals("Hardware solicitado", responseEntity.getBody().get(0).assunto());
            assertEquals("Seu pedido foi aprovado.", responseEntity.getBody().get(0).resposta());
            verify(pessoaRepository).findByEmail("usuario@teste.com");
            verify(suporteService).getTicketsByAutorId(1);
        }

        @Test
        @DisplayName("Retorna UNAUTHORIZED quando usuario nao existe no banco")
        void retornaUnauthorizedQuandoUsuarioNaoExiste() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.empty());

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        }

        @Test
        @DisplayName("Retorna lista vazia quando autor nao tem tickets")
        void retornaListaVaziaSemTickets() {
            User userDetails = new User("usuario@teste.com", "", List.of());
            when(pessoaRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(autor));
            when(suporteService.getTicketsByAutorId(1)).thenReturn(List.of());

            ResponseEntity<List<SuporteResponse>> responseEntity = suporteController.getMeusTickets(userDetails);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            assertNotNull(responseEntity.getBody());
            assertTrue(responseEntity.getBody().isEmpty());
        }
    }
}
