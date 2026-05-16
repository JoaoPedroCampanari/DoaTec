package com.doatec.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.doatec.dto.request.RespostaSuporteRequest;
import com.doatec.dto.response.AdminDashboardResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Aluno;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - Métodos Adicionais")
class AdminServiceTest {

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private SolicitacaoHardwareRepository solicitacaoRepository;

    @Mock
    private SuporteFormularioRepository suporteFormularioRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private LogAcaoRepository logAcaoRepository;

    @Mock
    private InventarioService inventarioService;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService service;

    private Pessoa admin;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        admin = new DoadorPF("123.456.789-00");
        admin.setId(1);
        admin.setNome("Admin Teste");
        admin.setEmail("admin@doatec.com");
        admin.setAtivo(true);
        admin.setRole(Role.ADMIN);

        pageable = PageRequest.of(0, 10);
    }

    // ==================== DASHBOARD ====================

    @Test
    @DisplayName("getDashboardStats retorna estatísticas corretas")
    void getDashboardStatsRetornaEstatisticas() {
        when(doacaoRepository.count()).thenReturn(50L);
        when(solicitacaoRepository.countByStatus(StatusSolicitacao.EM_ANALISE)).thenReturn(3L);
        when(suporteFormularioRepository.countByStatus(StatusSuporte.ABERTO)).thenReturn(7L);
        when(pessoaRepository.countByAtivoTrue()).thenReturn(100L);
        when(doacaoRepository.countByStatus(StatusDoacao.FINALIZADO)).thenReturn(40L);
        when(doacaoRepository.countByStatus(StatusDoacao.REJEITADA)).thenReturn(5L);
        when(solicitacaoRepository.countByStatus(StatusSolicitacao.APROVADA)).thenReturn(20L);
        when(solicitacaoRepository.countByStatus(StatusSolicitacao.REJEITADA)).thenReturn(2L);

        AdminDashboardResponse stats = service.getDashboardStats();

        assertEquals(50L, stats.totalDoacoes());
        assertEquals(3L, stats.totalSolicitacoesPendentes());
        assertEquals(7L, stats.totalTicketsAbertos());
        assertEquals(100L, stats.totalUsuariosAtivos());
        assertEquals(40L, stats.doacoesAprovadas());
        assertEquals(5L, stats.doacoesRejeitadas());
        assertEquals(20L, stats.solicitacoesAprovadas());
        assertEquals(2L, stats.solicitacoesRejeitadas());
    }

    // ==================== LISTAGENS ====================

    @Test
    @DisplayName("listarDoacoes com status retorna página filtrada")
    void listarDoacoesComStatus() {
        DoadorPF doador = new DoadorPF("111.222.333-44");
        doador.setId(10);
        doador.setNome("Doador Teste");
        doador.setEmail("doador@teste.com");

        Doacao doacao = new Doacao();
        doacao.setId(1);
        doacao.setStatus(StatusDoacao.EM_TRIAGEM);
        doacao.setDoador(doador);

        Page<Doacao> page = new PageImpl<>(List.of(doacao), pageable, 1);
        when(doacaoRepository.findByStatus(StatusDoacao.EM_TRIAGEM, pageable)).thenReturn(page);

        Page<DoacaoResponse> result = service.listarDoacoes(StatusDoacao.EM_TRIAGEM, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("EM_TRIAGEM", result.getContent().get(0).status());
        verify(doacaoRepository).findByStatus(StatusDoacao.EM_TRIAGEM, pageable);
    }

    @Test
    @DisplayName("listarSolicitacoes com status retorna página filtrada")
    void listarSolicitacoesComStatus() {
        Aluno aluno = new Aluno();
        aluno.setId(100);
        aluno.setNome("Aluno Teste");
        aluno.setEmail("aluno@teste.com");

        SolicitacaoHardware solicitacao = new SolicitacaoHardware();
        solicitacao.setId(10);
        solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
        solicitacao.setAluno(aluno);

        Page<SolicitacaoHardware> page = new PageImpl<>(List.of(solicitacao), pageable, 1);
        when(solicitacaoRepository.findByStatus(StatusSolicitacao.EM_ANALISE, pageable)).thenReturn(page);

        Page<SolicitacaoResponse> result = service.listarSolicitacoes(StatusSolicitacao.EM_ANALISE, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("EM_ANALISE", result.getContent().get(0).status());
        verify(solicitacaoRepository).findByStatus(StatusSolicitacao.EM_ANALISE, pageable);
    }

    @Test
    @DisplayName("listarTickets com status retorna página filtrada")
    void listarTicketsComStatus() {
        Pessoa autor = new DoadorPF("111.222.333-44");
        autor.setId(10);
        autor.setNome("Autor Teste");
        autor.setEmail("autor@teste.com");

        SuporteFormulario ticket = SuporteFormulario.builder()
                .id(1)
                .autor(autor)
                .assunto("Problema no sistema")
                .mensagem("Descrição detalhada do problema")
                .status(StatusSuporte.ABERTO)
                .build();

        Page<SuporteFormulario> page = new PageImpl<>(List.of(ticket), pageable, 1);
        when(suporteFormularioRepository.findByStatus(StatusSuporte.ABERTO, pageable)).thenReturn(page);

        Page<SuporteResponse> result = service.listarTickets(StatusSuporte.ABERTO, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("ABERTO", result.getContent().get(0).status());
        verify(suporteFormularioRepository).findByStatus(StatusSuporte.ABERTO, pageable);
    }

    @Test
    @DisplayName("listarTodosUsuarios retorna página de usuários")
    void listarTodosUsuarios() {
        Page<Pessoa> page = new PageImpl<>(List.of(admin), pageable, 1);
        when(pessoaRepository.findAll(pageable)).thenReturn(page);

        Page<UsuarioAdminResponse> result = service.listarTodosUsuarios(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Admin Teste", result.getContent().get(0).nome());
        verify(pessoaRepository).findAll(pageable);
    }

    // ==================== SUPORTE ====================

    @Nested
    @DisplayName("responderTicket")
    class ResponderTicketTests {

        @Test
        @DisplayName("responde ticket com sucesso e envia email")
        void respondeTicketComSucesso() {
            Pessoa autor = new DoadorPF("111.222.333-44");
            autor.setId(10);
            autor.setNome("Autor Teste");
            autor.setEmail("autor@teste.com");

            SuporteFormulario ticket = SuporteFormulario.builder()
                    .id(1)
                    .autor(autor)
                    .assunto("Problema no sistema")
                    .mensagem("Descrição do problema")
                    .status(StatusSuporte.ABERTO)
                    .build();

            RespostaSuporteRequest request = RespostaSuporteRequest.builder()
                    .resposta("Resposta do admin")
                    .build();

            when(suporteFormularioRepository.findById(1)).thenReturn(Optional.of(ticket));
            when(pessoaRepository.findById(admin.getId())).thenReturn(Optional.of(admin));
            when(suporteFormularioRepository.save(any(SuporteFormulario.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(logAcaoRepository.save(any())).thenReturn(null);

            SuporteResponse result = service.responderTicket(1, admin.getId(), request);

            assertEquals(StatusSuporte.RESOLVIDO.name(), result.status());
            assertEquals("Resposta do admin", ticket.getResposta());
            assertEquals(admin, ticket.getAdminResponsavel());
            assertNotNull(ticket.getDataResolucao());
            verify(emailService).enviarSuporteResposta(
                    eq("autor@teste.com"), eq("Autor Teste"),
                    eq("Problema no sistema"), eq("Resposta do admin"));
            verify(logAcaoRepository).save(any());
        }

        @Test
        @DisplayName("lança BusinessException quando ticket não encontrado")
        void ticketNaoEncontrado() {
            RespostaSuporteRequest request = RespostaSuporteRequest.builder()
                    .resposta("Resposta")
                    .build();

            when(suporteFormularioRepository.findById(999)).thenReturn(Optional.empty());

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> service.responderTicket(999, admin.getId(), request));

            assertTrue(ex.getMessage().contains("Ticket não encontrado"));
            verify(suporteFormularioRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("atualizarStatusTicket atualiza o status corretamente")
    void atualizarStatusTicket() {
        Pessoa autor = new DoadorPF("111.222.333-44");
        autor.setId(10);
        autor.setNome("Autor Teste");
        autor.setEmail("autor@teste.com");

        SuporteFormulario ticket = SuporteFormulario.builder()
                .id(1)
                .autor(autor)
                .assunto("Problema no sistema")
                .mensagem("Descrição do problema")
                .status(StatusSuporte.ABERTO)
                .build();

        when(suporteFormularioRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(suporteFormularioRepository.save(any(SuporteFormulario.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SuporteResponse result = service.atualizarStatusTicket(1, StatusSuporte.EM_ANDAMENTO);

        assertEquals(StatusSuporte.EM_ANDAMENTO.name(), result.status());
        assertEquals(StatusSuporte.EM_ANDAMENTO, ticket.getStatus());
        verify(suporteFormularioRepository).save(ticket);
    }

    // ==================== APROVAR DOAÇÃO ====================

    @Nested
    @DisplayName("aprovarDoacao")
    class AprovarDoacaoTests {

        @Test
        @DisplayName("aprova doação e notifica doador")
        void aprovaDoacaoENotifica() {
            DoadorPF doador = new DoadorPF("111.222.333-44");
            doador.setId(10);
            doador.setNome("Doador Teste");

            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.EM_ANALISE);
            doacao.setDoador(doador);
            doacao.setItens(new java.util.ArrayList<>());

            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacao);

            DoacaoResponse result = service.aprovarDoacao(1, 1, null);

            assertNotNull(result);
            assertEquals(StatusDoacao.FINALIZADO, doacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("DOACAO"));
            verify(logAcaoRepository).save(any());
        }
    }

    // ==================== REJEITAR DOAÇÃO ====================

    @Nested
    @DisplayName("rejeitarDoacao")
    class RejeitarDoacaoTests {

        @Test
        @DisplayName("rejeita doação e notifica doador")
        void rejeitaDoacaoENotifica() {
            DoadorPF doador = new DoadorPF("111.222.333-44");
            doador.setId(10);
            doador.setNome("Doador Teste");

            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.EM_ANALISE);
            doacao.setDoador(doador);

            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacao);

            DoacaoResponse result = service.rejeitarDoacao(1, 1, null);

            assertNotNull(result);
            assertEquals(StatusDoacao.REJEITADA, doacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("DOACAO"));
            verify(logAcaoRepository).save(any());
        }
    }

    // ==================== ALTERAR STATUS DOAÇÃO ====================

    @Nested
    @DisplayName("alterarStatusDoacao")
    class AlterarStatusDoacaoTests {

        @Test
        @DisplayName("altera status de doação com sucesso")
        void alteraStatusComSucesso() {
            DoadorPF doador = new DoadorPF("111.222.333-44");
            doador.setId(10);
            doador.setNome("Doador Teste");

            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.EM_TRIAGEM);
            doacao.setDoador(doador);

            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacao);

            DoacaoResponse result = service.alterarStatusDoacao(1, StatusDoacao.AGUARDANDO_COLETA, 1, null);

            assertNotNull(result);
            assertEquals(StatusDoacao.AGUARDANDO_COLETA, doacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("DOACAO"));
            verify(logAcaoRepository).save(any());
        }

        @Test
        @DisplayName("lança exceção para transição inválida")
        void lancaExcecaoParaTransicaoInvalida() {
            Doacao doacao = new Doacao();
            doacao.setId(1);
            doacao.setStatus(StatusDoacao.FINALIZADO);

            when(doacaoRepository.findById(1)).thenReturn(Optional.of(doacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));

            assertThrows(BusinessException.class,
                    () -> service.alterarStatusDoacao(1, StatusDoacao.EM_TRIAGEM, 1, null));
        }
    }

    // ==================== APROVAR SOLICITAÇÃO ====================

    @Nested
    @DisplayName("aprovarSolicitacao")
    class AprovarSolicitacaoTests {

        @Test
        @DisplayName("aprova solicitação e notifica aluno")
        void aprovaSolicitacaoENotifica() {
            Aluno aluno = new Aluno();
            aluno.setId(10);
            aluno.setNome("Aluno Teste");

            SolicitacaoHardware solicitacao = new SolicitacaoHardware();
            solicitacao.setId(1);
            solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
            solicitacao.setAluno(aluno);

            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.save(any(SolicitacaoHardware.class))).thenReturn(solicitacao);

            SolicitacaoResponse result = service.aprovarSolicitacao(1, 1, null);

            assertNotNull(result);
            assertEquals(StatusSolicitacao.APROVADA, solicitacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("SOLICITACAO"));
            verify(logAcaoRepository).save(any());
        }
    }

    // ==================== REJEITAR SOLICITAÇÃO ====================

    @Nested
    @DisplayName("rejeitarSolicitacao")
    class RejeitarSolicitacaoTests {

        @Test
        @DisplayName("rejeita solicitação e notifica aluno")
        void rejeitaSolicitacaoENotifica() {
            Aluno aluno = new Aluno();
            aluno.setId(10);
            aluno.setNome("Aluno Teste");

            SolicitacaoHardware solicitacao = new SolicitacaoHardware();
            solicitacao.setId(1);
            solicitacao.setStatus(StatusSolicitacao.EM_ANALISE);
            solicitacao.setAluno(aluno);

            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.save(any(SolicitacaoHardware.class))).thenReturn(solicitacao);

            SolicitacaoResponse result = service.rejeitarSolicitacao(1, 1, null);

            assertNotNull(result);
            assertEquals(StatusSolicitacao.REJEITADA, solicitacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("SOLICITACAO"));
            verify(logAcaoRepository).save(any());
        }
    }

    // ==================== CONCLUIR SOLICITAÇÃO ====================

    @Nested
    @DisplayName("concluirSolicitacao")
    class ConcluirSolicitacaoTests {

        @Test
        @DisplayName("conclui solicitação e notifica aluno")
        void concluiSolicitacaoENotifica() {
            Aluno aluno = new Aluno();
            aluno.setId(10);
            aluno.setNome("Aluno Teste");

            SolicitacaoHardware solicitacao = new SolicitacaoHardware();
            solicitacao.setId(1);
            solicitacao.setStatus(StatusSolicitacao.APROVADA);
            solicitacao.setAluno(aluno);

            when(solicitacaoRepository.findById(1)).thenReturn(Optional.of(solicitacao));
            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(solicitacaoRepository.save(any(SolicitacaoHardware.class))).thenReturn(solicitacao);

            SolicitacaoResponse result = service.concluirSolicitacao(1, 1, null);

            assertNotNull(result);
            assertEquals(StatusSolicitacao.CONCLUIDA, solicitacao.getStatus());
            verify(notificacaoService).criarNotificacao(eq(10), any(), any(), any(), eq(1), eq("SOLICITACAO"));
            verify(logAcaoRepository).save(any());
        }
    }

    // ==================== LISTAR USUÁRIOS POR TIPO ====================

    @Nested
    @DisplayName("listarUsuariosPorTipoPessoa")
    class ListarUsuariosPorTipoPessoaTests {

        @Test
        @DisplayName("lista usuários por tipo pessoa")
        void listaPorTipo() {
            when(pessoaRepository.findByTipo(any())).thenReturn(List.of(admin));

            List<UsuarioAdminResponse> result = service.listarUsuariosPorTipoPessoa(TipoPessoa.ALUNO);

            assertEquals(1, result.size());
            verify(pessoaRepository).findByTipo(any());
        }
    }

    // ==================== LISTAR USUÁRIOS POR ROLE ====================

    @Nested
    @DisplayName("listarUsuariosPorRole")
    class ListarUsuariosPorRoleTests {

        @Test
        @DisplayName("lista usuários por role")
        void listaPorRole() {
            when(pessoaRepository.findByRole(Role.ADMIN)).thenReturn(List.of(admin));

            List<UsuarioAdminResponse> result = service.listarUsuariosPorRole(Role.ADMIN);

            assertEquals(1, result.size());
            verify(pessoaRepository).findByRole(Role.ADMIN);
        }
    }

    // ==================== ALTERAR ROLE USUÁRIO ====================

    @Nested
    @DisplayName("alterarRoleUsuario")
    class AlterarRoleUsuarioTests {

        @Test
        @DisplayName("altera role de usuário com sucesso")
        void alteraRoleComSucesso() {
            Pessoa superAdmin = new DoadorPF("999.888.777-66");
            superAdmin.setId(99);
            superAdmin.setRole(Role.SUPER_ADMIN);

            Pessoa pessoa = new DoadorPF("111.222.333-44");
            pessoa.setId(2);
            pessoa.setRole(Role.USER);

            when(pessoaRepository.findById(99)).thenReturn(Optional.of(superAdmin));
            when(pessoaRepository.findById(2)).thenReturn(Optional.of(pessoa));
            when(pessoaRepository.save(any(Pessoa.class))).thenReturn(pessoa);

            UsuarioAdminResponse result = service.alterarRoleUsuario(2, Role.ADMIN, 99);

            assertNotNull(result);
            verify(pessoaRepository).save(pessoa);
        }

        @Test
        @DisplayName("lança exceção quando admin normal tenta promover a ADMIN")
        void lancaExcecaoQuandoAdminTentaPromover() {
            Pessoa pessoa = new DoadorPF("111.222.333-44");
            pessoa.setId(2);
            pessoa.setRole(Role.USER);

            when(pessoaRepository.findById(1)).thenReturn(Optional.of(admin));
            when(pessoaRepository.findById(2)).thenReturn(Optional.of(pessoa));

            assertThrows(BusinessException.class,
                    () -> service.alterarRoleUsuario(2, Role.ADMIN, 1));
        }
    }
}
