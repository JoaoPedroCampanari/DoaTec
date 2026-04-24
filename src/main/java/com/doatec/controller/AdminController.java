package com.doatec.controller;

import com.doatec.dto.request.AvaliacaoRequest;
import com.doatec.dto.request.RespostaSuporteRequest;
import com.doatec.dto.request.StatusUsuarioRequest;
import com.doatec.dto.response.AdminDashboardResponse;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.model.solicitacao.StatusSolicitacao;
import com.doatec.model.suporte.StatusSuporte;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.AdminService;
import com.doatec.service.PessoaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private PessoaRepository pessoaRepository;

    private Integer getAuthenticatedAdminId(User userDetails) {
        return pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"))
                .getId();
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        AdminDashboardResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    // ==================== DOAÇÕES ====================

    @GetMapping("/doacoes")
    public ResponseEntity<Page<DoacaoResponse>> listarDoacoes(
            @RequestParam(required = false) StatusDoacao status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<DoacaoResponse> doacoes = adminService.listarDoacoes(status, pageable);
        return ResponseEntity.ok(doacoes);
    }

    @PutMapping("/doacoes/{id}/aprovar")
    public ResponseEntity<DoacaoResponse> aprovarDoacao(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody(required = false) AvaliacaoRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        DoacaoResponse doacao = adminService.aprovarDoacao(id, adminId, request);
        return ResponseEntity.ok(doacao);
    }

    @PutMapping("/doacoes/{id}/rejeitar")
    public ResponseEntity<DoacaoResponse> rejeitarDoacao(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody(required = false) AvaliacaoRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        DoacaoResponse doacao = adminService.rejeitarDoacao(id, adminId, request);
        return ResponseEntity.ok(doacao);
    }

    // ==================== SOLICITAÇÕES ====================

    @GetMapping("/solicitacoes")
    public ResponseEntity<Page<SolicitacaoResponse>> listarSolicitacoes(
            @RequestParam(required = false) StatusSolicitacao status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<SolicitacaoResponse> solicitacoes = adminService.listarSolicitacoes(status, pageable);
        return ResponseEntity.ok(solicitacoes);
    }

    @PutMapping("/solicitacoes/{id}/aprovar")
    public ResponseEntity<SolicitacaoResponse> aprovarSolicitacao(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody(required = false) AvaliacaoRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        SolicitacaoResponse solicitacao = adminService.aprovarSolicitacao(id, adminId, request);
        return ResponseEntity.ok(solicitacao);
    }

    @PutMapping("/solicitacoes/{id}/rejeitar")
    public ResponseEntity<SolicitacaoResponse> rejeitarSolicitacao(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody(required = false) AvaliacaoRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        SolicitacaoResponse solicitacao = adminService.rejeitarSolicitacao(id, adminId, request);
        return ResponseEntity.ok(solicitacao);
    }

    // ==================== SUPORTE ====================

    @GetMapping("/suporte")
    public ResponseEntity<Page<SuporteResponse>> listarTickets(
            @RequestParam(required = false) StatusSuporte status,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<SuporteResponse> tickets = adminService.listarTickets(status, pageable);
        return ResponseEntity.ok(tickets);
    }

    @PutMapping("/suporte/{id}/responder")
    public ResponseEntity<SuporteResponse> responderTicket(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody RespostaSuporteRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        SuporteResponse ticket = adminService.responderTicket(id, adminId, request);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/suporte/{id}/status")
    public ResponseEntity<SuporteResponse> atualizarStatusTicket(
            @PathVariable Integer id,
            @RequestParam StatusSuporte status) {
        SuporteResponse ticket = adminService.atualizarStatusTicket(id, status);
        return ResponseEntity.ok(ticket);
    }

    // ==================== USUÁRIOS ====================

    @GetMapping("/usuarios")
    public ResponseEntity<Page<UsuarioAdminResponse>> listarTodosUsuarios(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UsuarioAdminResponse> usuarios = adminService.listarTodosUsuarios(pageable);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/tipo/{tipoPessoa}")
    public ResponseEntity<List<UsuarioAdminResponse>> listarUsuariosPorTipo(
            @PathVariable TipoPessoa tipoPessoa) {
        List<UsuarioAdminResponse> usuarios = adminService.listarUsuariosPorTipoPessoa(tipoPessoa);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/role/{role}")
    public ResponseEntity<List<UsuarioAdminResponse>> listarUsuariosPorRole(
            @PathVariable Role role) {
        List<UsuarioAdminResponse> usuarios = adminService.listarUsuariosPorRole(role);
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/usuarios/{id}/status")
    public ResponseEntity<UsuarioAdminResponse> alterarStatusUsuario(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody StatusUsuarioRequest request) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        UsuarioAdminResponse usuario = pessoaService.alterarStatusUsuario(id, request.ativo(), adminId);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/usuarios/{id}/role")
    public ResponseEntity<UsuarioAdminResponse> alterarRoleUsuario(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails,
            @RequestParam Role novaRole) {
        Integer adminId = getAuthenticatedAdminId(userDetails);
        UsuarioAdminResponse usuario = pessoaService.alterarRoleUsuario(id, novaRole, adminId);
        return ResponseEntity.ok(usuario);
    }
}