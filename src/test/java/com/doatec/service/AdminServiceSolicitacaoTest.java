package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.repository.*;
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
@DisplayName("AdminService - Transicoes de Status de Solicitacao")
class AdminServiceSolicitacaoTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @Mock
    private AlunoRepository alunoRepository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private LogAcaoRepository logAcaoRepository;

    @Mock
    private SuporteFormularioRepository suporteRepository;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;

    private Pessoa admin;
    private Pessoa superAdmin;
    private Aluno aluno;
    private SolicitacaoHardware solicitacao;

    @BeforeEach
    void setUp() {
        admin = new DoadorPF("123.456.789-00");
        admin.setId(1);
        admin.setNome("Admin Teste");
        admin.setEmail("admin@doatec.com");
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        superAdmin = new DoadorPF("987.654.321-00");
        superAdmin.setId(2);
        superAdmin.setNome("Super Admin");
        superAdmin.setEmail("super@doatec.com");
        superAdmin.setAtivo(true);
        superAdmin.setRole(Role.SUPER_ADMIN);

        aluno = new Aluno();
        aluno.setId(100);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");

        solicitacao = new SolicitacaoHardware();
        solicitacao.setId(10);
        solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
        solicitacao.setAluno(aluno);
    }

    private void stubSAVEMocks() {
        when(solicitacaoRepository.save(any(SolicitacaoHardware.class))).thenAnswer(inv -> inv.getArgument(0));
        when(logAcaoRepository.save(any())).thenReturn(null);
        when(notificacaoService.criarNotificacao(anyInt(), anyString(), anyString(), any(), anyInt(), anyString()))
                .thenReturn(null);
    }

    @Test
    void cancelaAprovadaSemEquipamentos() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of());
        stubSAVEMocks();
        var resposta = adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CANCELADA, admin.getId(), null);
        assertEquals("CANCELADA", resposta.status());
        verify(equipamentoRepository, never()).save(any());
    }

    @Test
    void cancelarAprovadaLiberaReservasENotifica() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var reservado = com.doatec.model.inventory.Equipamento.builder().id(8)
                .status(com.doatec.model.inventory.StatusEquipamento.RESERVADO)
                .solicitacaoDestino(solicitacao).alunoDestinatario(aluno)
                .dataAtribuicao(java.time.LocalDateTime.now()).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(reservado));
        stubSAVEMocks();
        var resposta = adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CANCELADA, admin.getId(), null);
        assertEquals("CANCELADA", resposta.status());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL, reservado.getStatus());
        assertNull(reservado.getSolicitacaoDestino());
        assertNull(reservado.getAlunoDestinatario());
        assertNull(reservado.getDataAtribuicao());
        verify(equipamentoRepository).save(reservado);
        verify(notificacaoService).criarNotificacao(eq(100), anyString(), contains("CANCELADA"), any(), eq(10), eq("SOLICITACAO"));
    }

    @Test
    void naoCancelaSeJaHaEquipamentoEntregue() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var reservado = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).solicitacaoDestino(solicitacao).build();
        var entregue = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.ENTREGUE).solicitacaoDestino(solicitacao).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(reservado, entregue));
        assertThrows(BusinessException.class, () -> adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CANCELADA, admin.getId(), null));
        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.RESERVADO, reservado.getStatus());
        verify(equipamentoRepository, never()).save(any());
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void solicitacaoCanceladaNaoPodeSerReaberta() {
        solicitacao.setStatus(StatusSolicitacao.CANCELADA);
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        assertThrows(BusinessException.class, () -> adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, admin.getId(), null));
    }

    @Test
    void aprovaComVariosEquipamentosDisponiveis() {
        var primeiro = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL).build();
        var segundo = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findByIdForUpdate(8)).thenReturn(Optional.of(primeiro));
        when(equipamentoRepository.findByIdForUpdate(9)).thenReturn(Optional.of(segundo));
        stubSAVEMocks();
        adminService.aprovarComEquipamentos(1, admin.getId(), java.util.List.of(9, 8), "Retirar no campus");
        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertEquals("Retirar no campus", solicitacao.getObservacaoAdmin());
        for (var equipamento : java.util.List.of(primeiro, segundo)) {
            assertEquals(com.doatec.model.inventory.StatusEquipamento.RESERVADO, equipamento.getStatus());
            assertSame(solicitacao, equipamento.getSolicitacaoDestino());
            assertSame(aluno, equipamento.getAlunoDestinatario());
        }
        verify(equipamentoRepository, times(2)).save(any());
        verify(notificacaoService).criarNotificacao(anyInt(), anyString(), anyString(), any(), anyInt(), anyString());
    }

    @Test
    void naoAprovaParcialmenteSeUmEquipamentoEstaIndisponivel() {
        var primeiro = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL).build();
        var segundo = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findByIdForUpdate(8)).thenReturn(Optional.of(primeiro));
        when(equipamentoRepository.findByIdForUpdate(9)).thenReturn(Optional.of(segundo));
        assertThrows(BusinessException.class, () -> adminService.aprovarComEquipamentos(1, admin.getId(), java.util.List.of(8, 9), null));
        assertEquals(StatusSolicitacao.EM_ANALISE, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL, primeiro.getStatus());
        verify(equipamentoRepository, never()).save(any());
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void naoAprovaSemEquipamentosOuComDuplicados() {
        assertThrows(BusinessException.class, () -> adminService.aprovarComEquipamentos(1, admin.getId(), java.util.List.of(), null));
        assertThrows(BusinessException.class, () -> adminService.aprovarComEquipamentos(1, admin.getId(), java.util.List.of(8, 8), null));
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void removerUltimoEquipamentoDevolveAoInventarioEVoltaParaAnalise() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var equipamento = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO)
                .solicitacaoDestino(solicitacao).alunoDestinatario(aluno).dataAtribuicao(java.time.LocalDateTime.now()).build();
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(equipamento));
        adminService.removerEquipamentoSolicitacao(1, 8, admin.getId(), true);
        assertEquals(StatusSolicitacao.EM_ANALISE, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL, equipamento.getStatus());
        assertNull(equipamento.getSolicitacaoDestino());
        assertNull(equipamento.getAlunoDestinatario());
        assertNull(equipamento.getDataAtribuicao());
        verify(notificacaoService).criarNotificacao(anyInt(), anyString(), anyString(), any(), anyInt(), anyString());
    }

    @Test
    void removerUmDeVariosMantemSolicitacaoAprovada() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var primeiro = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).solicitacaoDestino(solicitacao).build();
        var segundo = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).solicitacaoDestino(solicitacao).build();
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(primeiro, segundo));
        adminService.removerEquipamentoSolicitacao(1, 8, admin.getId(), false);
        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL, primeiro.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.RESERVADO, segundo.getStatus());
        verify(solicitacaoRepository, never()).save(any());
    }

    @Test
    void naoRemoveEquipamentoEntregueNemQuandoListaMudou() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var entregue = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.ENTREGUE).solicitacaoDestino(solicitacao).build();
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(entregue));
        assertThrows(BusinessException.class, () -> adminService.removerEquipamentoSolicitacao(1, 8, admin.getId(), true));
        entregue.setStatus(com.doatec.model.inventory.StatusEquipamento.RESERVADO);
        assertThrows(BusinessException.class, () -> adminService.removerEquipamentoSolicitacao(1, 8, admin.getId(), false));
        verify(equipamentoRepository, never()).save(any());
    }

    @Test
    void concluirSolicitacaoEntregaTodosOsReservadosEPreservaEntregues() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var reservado = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).solicitacaoDestino(solicitacao).build();
        var dataAnterior = java.time.LocalDateTime.of(2025, 1, 1, 10, 0);
        var entregue = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.ENTREGUE).dataEntrega(dataAnterior).solicitacaoDestino(solicitacao).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(reservado, entregue));
        stubSAVEMocks();
        adminService.concluirSolicitacao(1, admin.getId(), null);
        assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.ENTREGUE, reservado.getStatus());
        assertNotNull(reservado.getDataEntrega());
        assertEquals(dataAnterior, entregue.getDataEntrega());
        verify(equipamentoRepository).save(reservado);
        verify(equipamentoRepository, never()).save(entregue);
    }

    @Test
    void alterarStatusParaConcluidaEntregaEquipamentos() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var equipamento = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).solicitacaoDestino(solicitacao).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(equipamento));
        stubSAVEMocks();
        adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CONCLUIDA, admin.getId(), null);
        assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.ENTREGUE, equipamento.getStatus());
        assertNotNull(equipamento.getDataEntrega());
    }

    @Test
    void naoConcluiSeEquipamentoVinculadoEstaInvalido() {
        solicitacao.setStatus(StatusSolicitacao.APROVADA);
        var reservado = com.doatec.model.inventory.Equipamento.builder().id(8).status(com.doatec.model.inventory.StatusEquipamento.RESERVADO).build();
        var invalido = com.doatec.model.inventory.Equipamento.builder().id(9).status(com.doatec.model.inventory.StatusEquipamento.DISPONIVEL).build();
        when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
        when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
        when(equipamentoRepository.findBySolicitacaoDestinoIdForUpdate(1)).thenReturn(java.util.List.of(reservado, invalido));
        assertThrows(BusinessException.class, () -> adminService.concluirSolicitacao(1, admin.getId(), null));
        assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        assertEquals(com.doatec.model.inventory.StatusEquipamento.RESERVADO, reservado.getStatus());
        verify(equipamentoRepository, never()).save(any());
    }

    @Nested
    @DisplayName("validarTransicaoSolicitacao")
    class ValidarTransicaoTests {

        @Test
        @DisplayName("Aprovação direta exige equipamentos")
        void emAnaliseParaAprovada() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
            assertThrows(BusinessException.class, () -> adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, admin.getId(), null));
            assertEquals(StatusSolicitacao.EM_ANALISE, solicitacao.getStatus());
        }

        @Test
        @DisplayName("EM_ANALISE -> REJEITADA e valida")
        void emAnaliseParaRejeitada() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.REJEITADA, admin.getId(), null));

            assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
        }

        @Test
        @DisplayName("APROVADA -> CONCLUIDA e valida")
        void aprovadaParaConcluida() {
            solicitacao.setStatus(StatusSolicitacao.APROVADA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CONCLUIDA, admin.getId(), null));

            assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
        }

        @Test
        @DisplayName("Solicitacao rejeitada nao pode ser reaberta")
        void rejeitadaParaEmAnalise() {
            solicitacao.setStatus(StatusSolicitacao.REJEITADA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
            assertThrows(BusinessException.class, () ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.EM_ANALISE, admin.getId(), null));
            assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
            verify(solicitacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("CONCLUIDA -> qualquer status e invalida")
        void concluidaParaQualquerStatusInvalido() {
            solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.EM_ANALISE, admin.getId(), null));

            assertTrue(ex.getMessage().contains("Transição de status inválida"));
        }

        @Test
        @DisplayName("REJEITADA -> APROVADA e invalida")
        void rejeitadaParaAprovadaInvalido() {
            solicitacao.setStatus(StatusSolicitacao.REJEITADA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, admin.getId(), null));

            assertTrue(ex.getMessage().contains("Transição de status inválida"));
        }

        @Test
        @DisplayName("Super Admin pode alterar status de solicitacao")
        void superAdminPodeAlterarStatus() {
            when(pessoaRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));
            when(solicitacaoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.REJEITADA, superAdmin.getId(), null));

            assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
        }
    }
}
