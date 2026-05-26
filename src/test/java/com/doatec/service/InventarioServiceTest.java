package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.EquipamentoRequest;
import com.doatec.dto.response.EquipamentoResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.LogAcao;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.ItemDoado;
import com.doatec.model.inventory.Equipamento;
import com.doatec.model.inventory.EstadoConservacao;
import com.doatec.model.inventory.StatusEquipamento;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.DoacaoRepository;
import com.doatec.repository.EquipamentoRepository;
import com.doatec.repository.LogAcaoRepository;
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

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private LogAcaoRepository logAcaoRepository;

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
        admin.setRole(Role.ADMIN);

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
    // CRUD manual (etapas 3, 4 e 5)
    // =====================================================================

    @Nested
    @DisplayName("criarEquipamentoManual")
    class CriarEquipamentoManualTests {

        @Test
        @DisplayName("Sem doacaoId salva equipamento avulso e registra log")
        void semDoacao_salva() {
            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("HD")
                    .descricao("HD 500GB")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> {
                        Equipamento e = inv.getArgument(0);
                        e.setId(42);
                        return e;
                    });

            EquipamentoResponse resp = service.criarEquipamentoManual(req, 1);

            assertEquals(42, resp.id());
            assertEquals("HD", resp.tipo());
            assertNull(resp.doacaoId());
            verify(equipamentoRepository).save(any(Equipamento.class));
            verify(logAcaoRepository).save(any(LogAcao.class));
            verify(doacaoRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Com doacaoId valido salva com vinculo")
        void comDoacaoValida_salva() {
            Doacao doacao = Doacao.builder().id(7).build();
            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("RAM")
                    .descricao("DDR4 8GB")
                    .estadoConservacao(EstadoConservacao.NOVO)
                    .doacaoId(7)
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(doacaoRepository.findById(7)).thenReturn(Optional.of(doacao));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> {
                        Equipamento e = inv.getArgument(0);
                        e.setId(99);
                        return e;
                    });

            EquipamentoResponse resp = service.criarEquipamentoManual(req, 1);

            assertEquals(99, resp.id());
            assertEquals(7, resp.doacaoId());
            verify(doacaoRepository).findById(7);
            verify(logAcaoRepository).save(any(LogAcao.class));
        }

        @Test
        @DisplayName("doacaoId inexistente lanca BusinessException")
        void doacaoInexistente_lancaExcecao() {
            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("SSD")
                    .descricao("240GB")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .doacaoId(999)
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(doacaoRepository.findById(999)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.criarEquipamentoManual(req, 1));

            assertTrue(ex.getMessage().contains("999"));
            verify(equipamentoRepository, never()).save(any(Equipamento.class));
        }

        @Test
        @DisplayName("Usuario sem role admin lanca BusinessException")
        void usuarioSemRole_lancaExcecao() {
            Pessoa usuarioComum = new DoadorPF();
            usuarioComum.setId(50);
            usuarioComum.setRole(Role.USER);

            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("HD").descricao("x").estadoConservacao(EstadoConservacao.BOM)
                    .build();

            when(pessoaRepository.findById(50)).thenReturn(Optional.of(usuarioComum));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.criarEquipamentoManual(req, 50));

            assertTrue(ex.getMessage().toLowerCase().contains("administrador"));
        }
    }

    @Nested
    @DisplayName("atualizarEquipamento")
    class AtualizarEquipamentoTests {

        @Test
        @DisplayName("PUT em equipamento DISPONIVEL altera campos e troca vinculo")
        void disponivel_alteraTudo() {
            Doacao doacao = Doacao.builder().id(5).build();
            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("HD").descricao("nova descricao")
                    .estadoConservacao(EstadoConservacao.EXCELENTE)
                    .doacaoId(5)
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.findById(1)).thenReturn(Optional.of(equipamentoDisponivel));
            when(doacaoRepository.findById(5)).thenReturn(Optional.of(doacao));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            EquipamentoResponse resp = service.atualizarEquipamento(1, req, 1);

            assertEquals("nova descricao", resp.descricao());
            assertEquals(5, resp.doacaoId());
            verify(logAcaoRepository).save(any(LogAcao.class));
        }

        @Test
        @DisplayName("PUT mudando vinculo em equipamento RESERVADO lanca excecao (lock)")
        void reservadoMudaVinculo_lancaExcecao() {
            Equipamento reservado = Equipamento.builder()
                    .id(2).tipo("HD").descricao("x")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.RESERVADO)
                    .build();

            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("HD").descricao("x")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .doacaoId(5)  // muda de null para 5
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.findById(2)).thenReturn(Optional.of(reservado));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.atualizarEquipamento(2, req, 1));

            assertTrue(ex.getMessage().contains("vínculo"));
            verify(equipamentoRepository, never()).save(any(Equipamento.class));
        }

        @Test
        @DisplayName("PUT sem mudar vinculo em RESERVADO altera so descricao")
        void reservadoSemMudarVinculo_permite() {
            Equipamento reservado = Equipamento.builder()
                    .id(3).tipo("HD").descricao("antiga")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.RESERVADO)
                    .doacao(null)
                    .build();

            EquipamentoRequest req = EquipamentoRequest.builder()
                    .tipo("HD").descricao("corrigida")
                    .estadoConservacao(EstadoConservacao.REGULAR)
                    // doacaoId null = mantem
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.findById(3)).thenReturn(Optional.of(reservado));
            when(equipamentoRepository.save(any(Equipamento.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            EquipamentoResponse resp = service.atualizarEquipamento(3, req, 1);

            assertEquals("corrigida", resp.descricao());
            verify(doacaoRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("deletarEquipamento")
    class DeletarEquipamentoTests {

        @Test
        @DisplayName("DELETE em DISPONIVEL faz soft delete")
        void disponivel_deleta() {
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.findById(1)).thenReturn(Optional.of(equipamentoDisponivel));

            service.deletarEquipamento(1, 1);

            verify(equipamentoRepository).delete(equipamentoDisponivel);
            verify(logAcaoRepository).save(any(LogAcao.class));
        }

        @Test
        @DisplayName("DELETE em RESERVADO lanca BusinessException")
        void reservado_lancaExcecao() {
            Equipamento reservado = Equipamento.builder()
                    .id(2).tipo("HD").descricao("x")
                    .estadoConservacao(EstadoConservacao.BOM)
                    .status(StatusEquipamento.RESERVADO)
                    .build();

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(equipamentoRepository.findById(2)).thenReturn(Optional.of(reservado));

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.deletarEquipamento(2, 1));

            assertTrue(ex.getMessage().toLowerCase().contains("disponíveis") ||
                       ex.getMessage().toLowerCase().contains("disponiveis"));
            verify(equipamentoRepository, never()).delete(reservado);
        }
    }

    @Nested
    @DisplayName("listarEquipamentos (filtros)")
    class ListarComFiltrosTests {

        @Test
        @DisplayName("Filtro origem invalido lanca BusinessException")
        void origemInvalida_lancaExcecao() {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.listarEquipamentos(null, null, "XYZ", null, null));

            assertTrue(ex.getMessage().toLowerCase().contains("origem"));
        }

        @Test
        @DisplayName("Origem COM_VINCULO traduz para hasDoacao=TRUE no repo")
        void comVinculo_passaTrueAoRepo() {
            when(equipamentoRepository.findWithFilters(
                    any(), any(), eq(Boolean.TRUE), any(), any()))
                    .thenReturn(List.of(equipamentoDisponivel));

            List<EquipamentoResponse> result = service.listarEquipamentos(
                    null, null, "COM_VINCULO", null, null);

            assertEquals(1, result.size());
            verify(equipamentoRepository).findWithFilters(null, null, Boolean.TRUE, null, null);
        }

        @Test
        @DisplayName("doacaoId precede o filtro de origem")
        void doacaoId_precedeOrigem() {
            when(equipamentoRepository.findWithFilters(
                    any(), any(), eq((Boolean) null), eq(42), any()))
                    .thenReturn(List.of());

            service.listarEquipamentos(null, null, "SEM_VINCULO", 42, null);

            // hasDoacao deve ser null porque doacaoId tem precedencia
            verify(equipamentoRepository).findWithFilters(null, null, null, 42, null);
        }

        @Test
        @DisplayName("Parametro q em branco e tratado como null no repo")
        void qEmBranco_eIgnorado() {
            when(equipamentoRepository.findWithFilters(any(), any(), any(), any(), eq((String) null)))
                    .thenReturn(List.of());

            service.listarEquipamentos(null, null, null, null, "   ");

            verify(equipamentoRepository).findWithFilters(null, null, null, null, null);
        }

        @Test
        @DisplayName("Parametro q nao vazio e propagado (trimmed)")
        void qPropagado() {
            when(equipamentoRepository.findWithFilters(any(), any(), any(), any(), eq("HD")))
                    .thenReturn(List.of(equipamentoDisponivel));

            service.listarEquipamentos(null, null, null, null, "  HD  ");

            verify(equipamentoRepository).findWithFilters(null, null, null, null, "HD");
        }
    }
}
