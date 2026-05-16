package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.PessoaRepository;
import com.doatec.repository.SolicitacaoHardwareRepository;
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
@DisplayName("SolicitacaoService")
class SolicitacaoServiceTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoHardwareRepository;

    @InjectMocks
    private SolicitacaoService service;

    private Aluno aluno;
    private SolicitacaoRequest requestValido;

    @BeforeEach
    void setUp() {
        aluno = new Aluno();
        aluno.setId(1);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");
        aluno.setRa("RA12345");

        requestValido = SolicitacaoRequest.builder()
                .nome("Aluno Teste")
                .email("aluno@teste.com")
                .ra("RA12345")
                .justificativa("Preciso de um notebook para a faculdade")
                .preferenciaEquipamento("Notebook")
                .build();
    }

    // =====================================================================
    // criarSolicitacao
    // =====================================================================
    @Nested
    @DisplayName("criarSolicitacao")
    class CriarSolicitacaoTests {

        @Test
        @DisplayName("Deve lancar excecao quando email nao esta cadastrado")
        void emailNaoCadastrado_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("inexistente@email.com"))
                    .thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarSolicitacao("inexistente@email.com", requestValido));

            assertEquals("Usuário não encontrado.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao quando pessoa nao e Aluno")
        void pessoaNaoAluno_deveLancarExcecao() {
            DoadorPF doador = new DoadorPF("123.456.789-00");
            doador.setId(2);
            doador.setEmail("doador@teste.com");

            when(pessoaRepository.findByEmail("doador@teste.com"))
                    .thenReturn(Optional.of(doador));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarSolicitacao("doador@teste.com", requestValido));

            assertEquals("Apenas alunos podem fazer solicitações de hardware.", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lancar excecao quando RA informado nao corresponde ao cadastrado")
        void raDiferente_deveLancarExcecao() {
            when(pessoaRepository.findByEmail("aluno@teste.com"))
                    .thenReturn(Optional.of(aluno));

            SolicitacaoRequest requestRaErrado = SolicitacaoRequest.builder()
                    .nome("Aluno Teste")
                    .email("aluno@teste.com")
                    .ra("RA99999")
                    .justificativa("Preciso de um notebook")
                    .preferenciaEquipamento("Notebook")
                    .build();

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.criarSolicitacao("aluno@teste.com", requestRaErrado));

            assertTrue(ex.getMessage().contains("RA99999"));
            assertTrue(ex.getMessage().contains("não é válido"));
        }

        @Test
        @DisplayName("Deve criar solicitacao com sucesso e retornar entidade salva")
        void sucesso_deveRetornarSolicitacaoSalva() {
            when(pessoaRepository.findByEmail("aluno@teste.com"))
                    .thenReturn(Optional.of(aluno));
            when(solicitacaoHardwareRepository.save(any(SolicitacaoHardware.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            SolicitacaoHardware resultado = service.criarSolicitacao("aluno@teste.com", requestValido);

            assertNotNull(resultado);
            assertEquals(aluno, resultado.getAluno());
            assertEquals("Preciso de um notebook para a faculdade", resultado.getJustificativa());
            assertEquals("Notebook", resultado.getPreferenciaEquipamento());
            assertEquals(StatusSolicitacao.EM_ANALISE, resultado.getStatus());

            verify(solicitacaoHardwareRepository).save(any(SolicitacaoHardware.class));
        }
    }

    // =====================================================================
    // excluirSolicitacao
    // =====================================================================
    @Nested
    @DisplayName("excluirSolicitacao")
    class ExcluirSolicitacaoTests {

        private SolicitacaoHardware solicitacao;

        @BeforeEach
        void setUpSolicitacao() {
            solicitacao = new SolicitacaoHardware();
            solicitacao.setId(10);
            solicitacao.setAluno(aluno);
            solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
        }

        @Test
        @DisplayName("Deve lancar excecao quando status nao e EM_ANALISE")
        void statusNaoEmAnalise_deveLancarExcecao() {
            solicitacao.setStatus(StatusSolicitacao.APROVADA);

            when(solicitacaoHardwareRepository.findById(10))
                    .thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.excluirSolicitacao(10, "aluno@teste.com"));

            assertEquals("Só é possível excluir solicitações em análise.", ex.getMessage());

            verify(solicitacaoHardwareRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve lancar excecao quando solicitacao pertence a outro aluno")
        void ownershipDiferente_deveLancarExcecao() {
            when(solicitacaoHardwareRepository.findById(10))
                    .thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    service.excluirSolicitacao(10, "outro@email.com"));

            assertEquals("Você só pode excluir suas próprias solicitações.", ex.getMessage());

            verify(solicitacaoHardwareRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve excluir solicitacao com sucesso")
        void sucesso_deveExcluirSolicitacao() {
            when(solicitacaoHardwareRepository.findById(10))
                    .thenReturn(Optional.of(solicitacao));

            assertDoesNotThrow(() ->
                    service.excluirSolicitacao(10, "aluno@teste.com"));

            verify(solicitacaoHardwareRepository).delete(solicitacao);
        }
    }
}
