package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.model.account.Aluno;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SuporteFormularioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuporteFormularioService")
class SuporteFormularioServiceTest {

    @Mock
    private SuporteFormularioRepository suporteFormularioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private SuporteFormularioService service;

    private Aluno autor;
    private SuporteFormularioRequest requestValido;

    @BeforeEach
    void setUp() {
        autor = new Aluno();
        autor.setId(1);
        autor.setNome("Joao Silva");
        autor.setEmail("joao@teste.com");
        autor.setRa("RA12345");

        requestValido = SuporteFormularioRequest.builder()
                .nome("Joao Silva")
                .email("joao@teste.com")
                .assunto("Problema com login")
                .mensagem("Não consigo acessar minha conta")
                .build();
    }

    // =====================================================================
    // criarTicket
    // =====================================================================
    @Nested
    @DisplayName("criarTicket")
    class CriarTicketTests {

        @Test
        @DisplayName("Deve lancar excecao quando email nao esta cadastrado")
        void emailNaoCadastrado_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("inexistente@email.com"))
                    .thenReturn(Optional.empty());

            SuporteFormularioRequest requestEmailErrado = SuporteFormularioRequest.builder()
                    .nome("Fulano")
                    .email("inexistente@email.com")
                    .assunto("Teste")
                    .mensagem("Teste")
                    .build();

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.criarTicket(requestEmailErrado));

            assertEquals("Nenhuma conta encontrada com o email fornecido. Apenas usuários cadastrados podem abrir tickets de suporte.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao quando nome informado difere do cadastrado")
        void nomeDiferente_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("joao@teste.com"))
                    .thenReturn(Optional.of(autor));

            SuporteFormularioRequest requestNomeErrado = SuporteFormularioRequest.builder()
                    .nome("Nome Errado")
                    .email("joao@teste.com")
                    .assunto("Teste")
                    .mensagem("Teste")
                    .build();

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.criarTicket(requestNomeErrado));

            assertEquals("Nome inválido!", ex.getMessage());
        }

        @Test
        @DisplayName("Deve criar ticket com sucesso e retornar entidade salva")
        void sucesso_deveRetornarTicketSalvo() {
            when(pessoaRepository.findByEmail("joao@teste.com"))
                    .thenReturn(Optional.of(autor));
            when(suporteFormularioRepository.save(any(SuporteFormulario.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            SuporteFormulario resultado = service.criarTicket(requestValido);

            assertNotNull(resultado);
            assertEquals(autor, resultado.getAutor());
            assertEquals("Problema com login", resultado.getAssunto());
            assertEquals("Não consigo acessar minha conta", resultado.getMensagem());
            assertEquals(StatusSuporte.ABERTO, resultado.getStatus());

            verify(suporteFormularioRepository).save(any(SuporteFormulario.class));
        }
    }

    // =====================================================================
    // excluirTicket
    // =====================================================================
    @Nested
    @DisplayName("excluirTicket")
    class ExcluirTicketTests {

        private SuporteFormulario ticket;

        @BeforeEach
        void setUpTicket() {
            ticket = new SuporteFormulario();
            ticket.setId(10);
            ticket.setAutor(autor);
            ticket.setStatus(StatusSuporte.ABERTO);
            ticket.setAssunto("Teste");
            ticket.setMensagem("Mensagem");
        }

        @Test
        @DisplayName("Deve lancar excecao quando ticket nao existe")
        void ticketNaoExiste_deveLancarExcecao() {
            when(suporteFormularioRepository.findById(99))
                    .thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.excluirTicket(99, 1));

            assertEquals("Ticket não encontrado", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao quando autor nao e dono do ticket")
        void ownershipDiferente_deveLancarExcecao() {
            when(suporteFormularioRepository.findById(10))
                    .thenReturn(Optional.of(ticket));

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.excluirTicket(10, 999));

            assertEquals("Sem permissão para excluir este ticket", ex.getMessage());

            verify(suporteFormularioRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lancar excecao quando status nao e ABERTO")
        void statusNaoAberto_deveLancarExcecao() {
            ticket.setStatus(StatusSuporte.EM_ANDAMENTO);

            when(suporteFormularioRepository.findById(10))
                    .thenReturn(Optional.of(ticket));

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    service.excluirTicket(10, 1));

            assertEquals("Só é possível excluir tickets com status ABERTO", ex.getMessage());

            verify(suporteFormularioRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve excluir ticket com sucesso")
        void sucesso_deveExcluirTicket() {
            when(suporteFormularioRepository.findById(10))
                    .thenReturn(Optional.of(ticket));

            assertDoesNotThrow(() ->
                    service.excluirTicket(10, 1));

            verify(suporteFormularioRepository).deleteById(10);
        }
    }
}
