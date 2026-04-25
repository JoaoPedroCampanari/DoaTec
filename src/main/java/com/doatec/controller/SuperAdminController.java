package com.doatec.controller;

import com.doatec.dto.request.CriarAdminRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.exception.BusinessException;
import com.doatec.model.account.Role;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.SuperAdminService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-admin")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final PessoaRepository pessoaRepository;

    public SuperAdminController(SuperAdminService superAdminService, PessoaRepository pessoaRepository) {
        this.superAdminService = superAdminService;
        this.pessoaRepository = pessoaRepository;
    }

    private Integer getAuthenticatedSuperAdminId(@AuthenticationPrincipal User userDetails) {
        return pessoaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("Super Admin não encontrado"))
                .getId();
    }

    @GetMapping("/admins")
    public ResponseEntity<Page<UsuarioAdminResponse>> listarAdmins(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(superAdminService.listarAdmins(pageable));
    }

    @PostMapping("/admins")
    public ResponseEntity<UsuarioAdminResponse> criarAdmin(
            @Valid @RequestBody CriarAdminRequest request,
            @AuthenticationPrincipal User userDetails) {
        Integer superAdminId = getAuthenticatedSuperAdminId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(superAdminService.criarAdmin(request, superAdminId));
    }

    @PutMapping("/admins/{id}/rebaixar")
    public ResponseEntity<UsuarioAdminResponse> rebaixarAdmin(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails) {
        Integer superAdminId = getAuthenticatedSuperAdminId(userDetails);
        return ResponseEntity.ok(superAdminService.rebaixarAdmin(id, superAdminId));
    }

    @PutMapping("/admins/{id}/role")
    public ResponseEntity<UsuarioAdminResponse> alterarRoleAdmin(
            @PathVariable Integer id,
            @RequestParam Role novaRole,
            @AuthenticationPrincipal User userDetails) {
        Integer superAdminId = getAuthenticatedSuperAdminId(userDetails);
        return ResponseEntity.ok(superAdminService.alterarRoleAdmin(id, novaRole, superAdminId));
    }

    @PutMapping("/admins/{id}/status")
    public ResponseEntity<UsuarioAdminResponse> alterarStatusAdmin(
            @PathVariable Integer id,
            @RequestBody java.util.Map<String, Boolean> body,
            @AuthenticationPrincipal User userDetails) {
        Integer superAdminId = getAuthenticatedSuperAdminId(userDetails);
        Boolean ativo = body.get("ativo");
        if (ativo == null) {
            throw new BusinessException("Campo 'ativo' é obrigatório.");
        }
        return ResponseEntity.ok(superAdminService.alterarStatusAdmin(id, ativo, superAdminId));
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> excluirAdmin(
            @PathVariable Integer id,
            @AuthenticationPrincipal User userDetails) {
        Integer superAdminId = getAuthenticatedSuperAdminId(userDetails);
        superAdminService.excluirAdmin(id, superAdminId);
        return ResponseEntity.noContent().build();
    }
}
