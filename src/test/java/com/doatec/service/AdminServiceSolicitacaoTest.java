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
    private InventarioService inventarioService;

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

    @Nested
    @DisplayName("validarTransicaoSolicitacao")
    class ValidarTransicaoTests {

        @Test
        @DisplayName("EM_ANALISE -> APROVADA e valida")
        void emAnaliseParaAprovada() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, admin.getId(), null));

            assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        }

        @Test
        @DisplayName("EM_ANALISE -> REJEITADA e valida")
        void emAnaliseParaRejeitada() {
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
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
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.CONCLUIDA, admin.getId(), null));

            assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
        }

        @Test
        @DisplayName("REJEITADA -> EM_ANALISE e valida (undo rejeicao)")
        void rejeitadaParaEmAnalise() {
            solicitacao.setStatus(StatusSolicitacao.REJEITADA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.EM_ANALISE, admin.getId(), null));

            assertEquals(StatusSolicitacao.EM_ANALISE, solicitacao.getStatus());
        }

        @Test
        @DisplayName("CONCLUIDA -> qualquer status e invalida")
        void concluidaParaQualquerStatusInvalido() {
            solicitacao.setStatus(StatusSolicitacao.CONCLUIDA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.EM_ANALISE, admin.getId(), null));

            assertTrue(ex.getMessage().contains("Transição de status inválida"));
        }

        @Test
        @DisplayName("REJEITADA -> APROVADA e invalida")
        void rejeitadaParaAprovadaInvalido() {
            solicitacao.setStatus(StatusSolicitacao.REJEITADA);
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));

            BusinessException ex = assertThrows(BusinessException.class, () ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, admin.getId(), null));

            assertTrue(ex.getMessage().contains("Transição de status inválida"));
        }

        @Test
        @DisplayName("Super Admin pode alterar status de solicitacao")
        void superAdminPodeAlterarStatus() {
            when(pessoaRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));
            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            stubSAVEMocks();

            assertDoesNotThrow(() ->
                    adminService.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, superAdmin.getId(), null));

            assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
        }
    }
}
