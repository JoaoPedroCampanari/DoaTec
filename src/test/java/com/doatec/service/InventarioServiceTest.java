package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.dto.response.SugestaoMatchingResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.EquipamentoRepository;
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

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventarioService")
class InventarioServiceTest {

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @InjectMocks
    private InventarioService service;

    private ItemDoado itemDoado;
    private Pessoa admin;
    private Aluno aluno;
    private SolicitacaoHardware solicitacao;
    private Equipamento equipamentoDisponivel;

    @BeforeEach
    void setUp() {
        itemDoado = ItemDoado.builder()
                .id(1)
                .tipoItem("Notebook")
                .descricao("Notebook Dell i5 8GB")
                .build();

        admin = new DoadorPF();
        admin.setId(1);
        admin.setNome("Admin Teste");

        aluno = Aluno.builder()
                .id(10)
                .nome("Aluno Teste")
                .email("aluno@teste.com")
                .ra("2024001")
                .build();

        solicitacao = SolicitacaoHardware.builder()
                .id(100)
                .aluno(aluno)
                .status(StatusSolicitacao.EM_ANALISE)
                .preferenciaEquipamento("Notebook")
                .build();

        equipamentoDisponivel = Equipamento.builder()
                .id(1)
                .tipo("Notebook")
                .descricao("Notebook Dell i5 8GB")
                .estadoConservacao(EstadoConservacao.BOM)
                .status(StatusEquipamento.DISPONIVEL)
                .build();
    }

    // =====================================================================
    // 1. criarEquipamento
    // =====================================================================

    @Nested
    @DisplayName("criarEquipamento")
    class CriarEquipamentoTests {

        @Test
        @DisplayName("Estado nulo assume BOM como padrao")
        void estadoNulo_assumeBom() {
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Equipamento result = service.criarEquipamento(itemDoado, null);

            assertEquals(EstadoConservacao.BOM, result.getEstadoConservacao());
            assertEquals(StatusEquipamento.DISPONIVEL, result.getStatus());
            assertEquals("Notebook", result.getTipo());
            assertEquals("Notebook Dell i5 8GB", result.getDescricao());
            assertEquals(itemDoado, result.getItemOrigem());
            verify(equipamentoRepository).save(any(Equipamento.class));
        }

        @Test
        @DisplayName("Estado fornecido e utilizado corretamente")
        void estadoFornecido_utilizaFornecido() {
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            Equipamento result = service.criarEquipamento(itemDoado, EstadoConservacao.EXCELENTE);

            assertEquals(EstadoConservacao.EXCELENTE, result.getEstadoConservacao());
            assertEquals(StatusEquipamento.DISPONIVEL, result.getStatus());
            verify(equipamentoRepository).save(any(Equipamento.class));
        }
    }

    // =====================================================================
    // 2. atribuirEquipamento
    // =====================================================================

    @Nested
    @DisplayName("atribuirEquipamento")
    class AtribuirEquipamentoTests {

        @Test
        @DisplayName("Equipamento nao DISPONIVEL lanca excecao")
        void equipamentoNaoDisponivel_lancaExcecao() {
            Equipamento reservado = Equipamento.builder()
                    .id(2)
                    .tipo("Notebook")
                    .descricao("Notebook reservado")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.RESERVADO)
                    .build();

            when(equipamentoRepository.findById(2)).thenReturn(Optional.of(reservado));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atribuirEquipamento(2, 100, 1));

            assertTrue(ex.getMessage().contains("não está disponível"));
        }

        @Test
        @DisplayName("Solicitacao sem aluno do tipo Aluno lanca excecao")
        void solicitacaoSemAluno_lancaExcecao() {
            Pessoa doador = new DoadorPF();
            doador.setId(20);
            doador.setNome("Doador Teste");

            SolicitacaoHardware solicitacaoInvalida = SolicitacaoHardware.builder()
                    .id(101)
                    .aluno(doador)
                    .status(StatusSolicitacao.APROVADA)
                    .build();

            when(equipamentoRepository.findById(1)).thenReturn(Optional.of(equipamentoDisponivel));
            when(solicitacaoRepository.findById(101)).thenReturn(Optional.of(solicitacaoInvalida));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atribuirEquipamento(1, 101, 1));

            assertTrue(ex.getMessage().contains("não pertence a um aluno"));
        }

        @Test
        @DisplayName("Sucesso: equipamento vira RESERVADO com dados da solicitacao")
        void sucesso_equipamentoReservado() {
            when(equipamentoRepository.findById(1)).thenReturn(Optional.of(equipamentoDisponivel));
            when(solicitacaoRepository.findById(100)).thenReturn(Optional.of(solicitacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            EquipamentoResponse response = service.atribuirEquipamento(1, 100, 1);

            assertEquals(StatusEquipamento.RESERVADO.name(), response.status());
            assertEquals(100, response.solicitacaoDestinoId());
            assertEquals(aluno.getId(), response.alunoDestinatarioId());
            verify(equipamentoRepository).save(any(Equipamento.class));
        }
    }

    // =====================================================================
    // 3. marcarComoEntregue
    // =====================================================================

    @Nested
    @DisplayName("marcarComoEntregue")
    class MarcarComoEntregueTests {

        @Test
        @DisplayName("Equipamento nao RESERVADO lanca excecao")
        void equipamentoNaoReservado_lancaExcecao() {
            when(equipamentoRepository.findById(1)).thenReturn(Optional.of(equipamentoDisponivel));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.marcarComoEntregue(1, 1));

            assertTrue(ex.getMessage().contains("reservados"));
        }

        @Test
        @DisplayName("Sucesso: equipamento vira ENTREGUE")
        void sucesso_equipamentoEntregue() {
            Equipamento reservado = Equipamento.builder()
                    .id(2)
                    .tipo("Notebook")
                    .descricao("Notebook reservado")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.RESERVADO)
                    .build();

            when(equipamentoRepository.findById(2)).thenReturn(Optional.of(reservado));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            EquipamentoResponse response = service.marcarComoEntregue(2, 1);

            assertEquals(StatusEquipamento.ENTREGUE.name(), response.status());
            verify(equipamentoRepository).save(any(Equipamento.class));
        }
    }

    // =====================================================================
    // 4. sugerirMatchings
    // =====================================================================

    @Nested
    @DisplayName("sugerirMatchings")
    class SugerirMatchingsTests {

        @Test
        @DisplayName("Match exato de descricao retorna score alto (100)")
        void matchExato_scoreAlto() {
            Equipamento matchExato = Equipamento.builder()
                    .id(3)
                    .tipo("Notebook")
                    .descricao("Notebook para uso")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.DISPONIVEL)
                    .build();

            when(solicitacaoRepository.findById(100)).thenReturn(Optional.of(solicitacao));
            when(equipamentoRepository.findDisponiveisByKeyword("Notebook"))
                    .thenReturn(List.of(matchExato));

            SugestaoMatchingResponse response = service.sugerirMatchings(100);

            assertEquals(1, response.equipamentosCompativeis().size());
            assertEquals(100, response.equipamentosCompativeis().get(0).scoreCompatibilidade());
        }

        @Test
        @DisplayName("Match parcial retorna score medio")
        void matchParcial_scoreMedio() {
            Equipamento matchParcial = Equipamento.builder()
                    .id(4)
                    .tipo("Notebook Dell Inspiron")
                    .descricao("Notebook Dell para estudante")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.DISPONIVEL)
                    .build();

            when(solicitacaoRepository.findById(100)).thenReturn(Optional.of(solicitacao));
            when(equipamentoRepository.findDisponiveisByKeyword("Notebook"))
                    .thenReturn(List.of(matchParcial));

            SugestaoMatchingResponse response = service.sugerirMatchings(100);

            assertEquals(1, response.equipamentosCompativeis().size());
            Integer score = response.equipamentosCompativeis().get(0).scoreCompatibilidade();
            assertEquals(80, score);
        }

        @Test
        @DisplayName("Match por palavra-chave retorna score medio-baixo (50 + 10)")
        void matchPorPalavraChave_scoreMedioBaixo() {
            Equipamento matchKeyword = Equipamento.builder()
                    .id(5)
                    .tipo("Dell Inspiron")
                    .descricao("Dell Inspiron para estudante")
                    .estadoConservacao(EstadoConservacao.REGULAR)
                    .status(StatusEquipamento.DISPONIVEL)
                    .build();

            SolicitacaoHardware solicitacaoMulti = SolicitacaoHardware.builder()
                    .id(100)
                    .aluno(aluno)
                    .status(StatusSolicitacao.EM_ANALISE)
                    .preferenciaEquipamento("Dell Notebook")
                    .build();

            when(solicitacaoRepository.findById(100)).thenReturn(Optional.of(solicitacaoMulti));
            when(equipamentoRepository.findDisponiveisByKeyword("Dell Notebook"))
                    .thenReturn(List.of(matchKeyword));

            SugestaoMatchingResponse response = service.sugerirMatchings(100);

            assertEquals(1, response.equipamentosCompativeis().size());
            Integer score = response.equipamentosCompativeis().get(0).scoreCompatibilidade();
            // pref=["dell","notebook"], tipo=["dell","inspiron"]
            // "dell" matches "dell" => matches=1 => 50 + 10 = 60
            assertEquals(60, score);
        }

        @Test
        @DisplayName("Sem match retorna score baixo (20)")
        void semMatch_scoreBaixo() {
            Equipamento semMatch = Equipamento.builder()
                    .id(6)
                    .tipo("Impressora")
                    .descricao("Impressora HP")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.DISPONIVEL)
                    .build();

            when(solicitacaoRepository.findById(100)).thenReturn(Optional.of(solicitacao));
            when(equipamentoRepository.findDisponiveisByKeyword("Notebook"))
                    .thenReturn(List.of(semMatch));

            SugestaoMatchingResponse response = service.sugerirMatchings(100);

            assertEquals(1, response.equipamentosCompativeis().size());
            Integer score = response.equipamentosCompativeis().get(0).scoreCompatibilidade();
            assertEquals(20, score);
        }
    }
}
