package com.doatec.controller;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.mapper.DoacaoMapper;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.service.DoacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller para gerenciamento das doações.
 */
@RestController
@RequestMapping("/api/donations")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    /**
     * Lista doações com filtros opcionais.
     */
    @GetMapping
    public ResponseEntity<List<DoacaoResponse>> listDonations(
            @RequestParam(required = false) StatusDoacao status,
            @RequestParam(required = false) Integer doadorId) {
        List<DoacaoResponse> doacoes = doacaoService.listDoacoes(status, doadorId);
        return ResponseEntity.ok(doacoes);
    }

    /**
     * Registra uma doação do usuário autenticado.
     */
    @PostMapping
    public ResponseEntity<DoacaoResponse> createDonation(@Valid @RequestBody DoacaoRequest doacaoRequest,
                                                          @AuthenticationPrincipal User userDetails) {
        if (userDetails == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (!userDetails.getUsername().equalsIgnoreCase(doacaoRequest.email())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Doacao novaDoacao = doacaoService.registrarDoacao(doacaoRequest);
        DoacaoResponse response = DoacaoMapper.toResponse(novaDoacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Exclui uma doação do usuário autenticado.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id, @AuthenticationPrincipal User userDetails) {
        doacaoService.excluirDoacao(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}