package com.doatec.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.dto.request.RespostaSuporteRequest;
import com.doatec.dto.request.StatusUsuarioRequest;
import com.doatec.dto.response.AdminDashboardResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.model.account.DoadorPF;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.AdminService;
import com.doatec.service.PessoaService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController")
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @Mock
    private PessoaService pessoaService;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private AdminController controller;

    private Pessoa admin;
    private User userDetails;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        admin = DoadorPF.builder()
                .id(1)
                .nome("Admin Teste")
                .email("admin@test.com")
                .cpf("123.456.789-00")
                .role(Role.ADMIN)
                .ativo(true)
                .build();

        userDetails = new User("admin@test.com", "password", List.of());
        pageable = PageRequest.of(0, 20);
    }

    private void mockAuthenticatedAdmin() {
        when(pessoaRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
    }

    // ==================== DASHBOARD ====================

    @Nested
    @DisplayName("Dashboard")
    class DashboardTests {

        @Test
        @DisplayName("GET /dashboard retorna 200 com estatísticas")
        void getDashboard_retorna200() {
            AdminDashboardResponse stats = AdminDashboardResponse.builder()
                    .totalDoacoes(10L)
                    .totalSolicitacoesPendentes(3L)
                    .totalTicketsAbertos(5L)
                    .totalUsuariosAtivos(50L)
                    .doacoesAprovadas(7L)
                    .doacoesRejeitadas(2L)
                    .solicitacoesAprovadas(4L)
                    .solicitacoesRejeitadas(1L)
                    .build();

            when(adminService.getDashboardStats()).thenReturn(stats);

            ResponseEntity<AdminDashboardResponse> response = controller.getDashboard();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(10L, response.getBody().totalDoacoes());
        }
    }

    // ==================== DOAÇÕES ====================

    @Nested
    @DisplayName("Doações")
    class DoacoesTests {

        @Test
        @DisplayName("GET /doacoes retorna 200 com página")
        void listarDoacoes_retorna200() {
            DoacaoResponse doacao = DoacaoResponse.builder()
                    .id(1)
                    .status("EM_TRIAGEM")
                    .build();

            Page<DoacaoResponse> page = new PageImpl<>(List.of(doacao), pageable, 1);
            when(adminService.listarDoacoes(any(), any())).thenReturn(page);

            ResponseEntity<Page<DoacaoResponse>> response = controller.listarDoacoes(null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getTotalElements());
        }

        @Test
        @DisplayName("PUT /doacoes/{id}/aprovar retorna 200")
        void aprovarDoacao_retorna200() {
            mockAuthenticatedAdmin();
            DoacaoResponse doacao = DoacaoResponse.builder().id(1).status("FINALIZADO").build();
            when(adminService.aprovarDoacao(eq(1), eq(1), any())).thenReturn(doacao);

            ResponseEntity<DoacaoResponse> response = controller.aprovarDoacao(1, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("FINALIZADO", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /doacoes/{id}/rejeitar retorna 200")
        void rejeitarDoacao_retorna200() {
            mockAuthenticatedAdmin();
            DoacaoResponse doacao = DoacaoResponse.builder().id(1).status("REJEITADA").build();
            when(adminService.rejeitarDoacao(eq(1), eq(1), any())).thenReturn(doacao);

            ResponseEntity<DoacaoResponse> response = controller.rejeitarDoacao(1, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("REJEITADA", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /doacoes/{id}/status retorna 200")
        void alterarStatusDoacao_retorna200() {
            mockAuthenticatedAdmin();
            DoacaoResponse doacao = DoacaoResponse.builder().id(1).status("EM_ANALISE").build();
            when(adminService.alterarStatusDoacao(eq(1), eq(StatusDoacao.EM_ANALISE), eq(1), any())).thenReturn(doacao);

            ResponseEntity<DoacaoResponse> response = controller.alterarStatusDoacao(1, StatusDoacao.EM_ANALISE, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("EM_ANALISE", response.getBody().status());
        }
    }

    // ==================== SOLICITAÇÕES ====================

    @Nested
    @DisplayName("Solicitações")
    class SolicitacoesTests {

        @Test
        @DisplayName("GET /solicitacoes retorna 200 com página")
        void listarSolicitacoes_retorna200() {
            SolicitacaoResponse sol = SolicitacaoResponse.builder().id(1).status("EM_ANALISE").build();
            Page<SolicitacaoResponse> page = new PageImpl<>(List.of(sol), pageable, 1);
            when(adminService.listarSolicitacoes(any(), any())).thenReturn(page);

            ResponseEntity<Page<SolicitacaoResponse>> response = controller.listarSolicitacoes(null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getTotalElements());
        }

        @Test
        @DisplayName("PUT /solicitacoes/{id}/aprovar retorna 200")
        void aprovarSolicitacao_retorna200() {
            mockAuthenticatedAdmin();
            SolicitacaoResponse sol = SolicitacaoResponse.builder().id(1).status("APROVADA").build();
            when(adminService.aprovarSolicitacao(eq(1), eq(1), any())).thenReturn(sol);

            ResponseEntity<SolicitacaoResponse> response = controller.aprovarSolicitacao(1, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("APROVADA", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /solicitacoes/{id}/rejeitar retorna 200")
        void rejeitarSolicitacao_retorna200() {
            mockAuthenticatedAdmin();
            SolicitacaoResponse sol = SolicitacaoResponse.builder().id(1).status("REJEITADA").build();
            when(adminService.rejeitarSolicitacao(eq(1), eq(1), any())).thenReturn(sol);

            ResponseEntity<SolicitacaoResponse> response = controller.rejeitarSolicitacao(1, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("REJEITADA", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /solicitacoes/{id}/status retorna 200")
        void alterarStatusSolicitacao_retorna200() {
            mockAuthenticatedAdmin();
            SolicitacaoResponse sol = SolicitacaoResponse.builder().id(1).status("APROVADA").build();
            when(adminService.alterarStatusSolicitacao(eq(1), eq(StatusSolicitacao.APROVADA), eq(1), any())).thenReturn(sol);

            ResponseEntity<SolicitacaoResponse> response = controller.alterarStatusSolicitacao(1, StatusSolicitacao.APROVADA, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("APROVADA", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /solicitacoes/{id}/concluir retorna 200")
        void concluirSolicitacao_retorna200() {
            mockAuthenticatedAdmin();
            SolicitacaoResponse sol = SolicitacaoResponse.builder().id(1).status("CONCLUIDA").build();
            when(adminService.concluirSolicitacao(eq(1), eq(1), any())).thenReturn(sol);

            ResponseEntity<SolicitacaoResponse> response = controller.concluirSolicitacao(1, userDetails, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("CONCLUIDA", response.getBody().status());
        }
    }

    // ==================== SUPORTE ====================

    @Nested
    @DisplayName("Suporte")
    class SuporteTests {

        @Test
        @DisplayName("GET /suporte retorna 200 com página")
        void listarTickets_retorna200() {
            SuporteResponse ticket = SuporteResponse.builder().id(1).status("ABERTO").build();
            Page<SuporteResponse> page = new PageImpl<>(List.of(ticket), pageable, 1);
            when(adminService.listarTickets(any(), any())).thenReturn(page);

            ResponseEntity<Page<SuporteResponse>> response = controller.listarTickets(null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getTotalElements());
        }

        @Test
        @DisplayName("PUT /suporte/{id}/responder retorna 200")
        void responderTicket_retorna200() {
            mockAuthenticatedAdmin();
            SuporteResponse ticket = SuporteResponse.builder().id(1).status("RESOLVIDO").build();
            RespostaSuporteRequest request = RespostaSuporteRequest.builder().resposta("Resposta").build();
            when(adminService.responderTicket(eq(1), eq(1), any())).thenReturn(ticket);

            ResponseEntity<SuporteResponse> response = controller.responderTicket(1, userDetails, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("RESOLVIDO", response.getBody().status());
        }

        @Test
        @DisplayName("PUT /suporte/{id}/status retorna 200")
        void atualizarStatusTicket_retorna200() {
            SuporteResponse ticket = SuporteResponse.builder().id(1).status("EM_ANDAMENTO").build();
            when(adminService.atualizarStatusTicket(1, StatusSuporte.EM_ANDAMENTO)).thenReturn(ticket);

            ResponseEntity<SuporteResponse> response = controller.atualizarStatusTicket(1, StatusSuporte.EM_ANDAMENTO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("EM_ANDAMENTO", response.getBody().status());
        }
    }

    // ==================== USUÁRIOS ====================

    @Nested
    @DisplayName("Usuários")
    class UsuariosTests {

        @Test
        @DisplayName("GET /usuarios retorna 200 com página")
        void listarTodosUsuarios_retorna200() {
            UsuarioAdminResponse user = UsuarioAdminResponse.builder().id(1).nome("User").build();
            Page<UsuarioAdminResponse> page = new PageImpl<>(List.of(user), pageable, 1);
            when(adminService.listarTodosUsuarios(any())).thenReturn(page);

            ResponseEntity<Page<UsuarioAdminResponse>> response = controller.listarTodosUsuarios(pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getTotalElements());
        }

        @Test
        @DisplayName("GET /usuarios/tipo/{tipoPessoa} retorna 200")
        void listarUsuariosPorTipo_retorna200() {
            UsuarioAdminResponse user = UsuarioAdminResponse.builder().id(1).nome("Aluno").build();
            when(adminService.listarUsuariosPorTipoPessoa(TipoPessoa.ALUNO)).thenReturn(List.of(user));

            ResponseEntity<List<UsuarioAdminResponse>> response = controller.listarUsuariosPorTipo(TipoPessoa.ALUNO);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("GET /usuarios/role/{role} retorna 200")
        void listarUsuariosPorRole_retorna200() {
            UsuarioAdminResponse user = UsuarioAdminResponse.builder().id(1).nome("Admin").build();
            when(adminService.listarUsuariosPorRole(Role.ADMIN)).thenReturn(List.of(user));

            ResponseEntity<List<UsuarioAdminResponse>> response = controller.listarUsuariosPorRole(Role.ADMIN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().size());
        }

        @Test
        @DisplayName("PUT /usuarios/{id}/status retorna 200")
        void alterarStatusUsuario_retorna200() {
            mockAuthenticatedAdmin();
            UsuarioAdminResponse user = UsuarioAdminResponse.builder().id(2).ativo(false).build();
            StatusUsuarioRequest request = new StatusUsuarioRequest(false);
            when(pessoaService.alterarStatusUsuario(2, false, 1)).thenReturn(user);

            ResponseEntity<UsuarioAdminResponse> response = controller.alterarStatusUsuario(2, userDetails, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertFalse(response.getBody().ativo());
        }

        @Test
        @DisplayName("PUT /usuarios/{id}/role retorna 200")
        void alterarRoleUsuario_retorna200() {
            mockAuthenticatedAdmin();
            UsuarioAdminResponse user = UsuarioAdminResponse.builder().id(2).role("ADMIN").build();
            when(pessoaService.alterarRoleUsuario(2, Role.ADMIN, 1)).thenReturn(user);

            ResponseEntity<UsuarioAdminResponse> response = controller.alterarRoleUsuario(2, userDetails, Role.ADMIN);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("ADMIN", response.getBody().role());
        }
    }
}
