package com.doatec.controller;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.dto.response.SolicitacaoResponse;
import com.doatec.mapper.SolicitacaoMapper;
import com.doatec.model.solicitacao.SolicitacaoHardware;
import com.doatec.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<SolicitacaoResponse> criar(
            @AuthenticationPrincipal User userDetails,
            @Valid @RequestBody SolicitacaoRequest dto) {
        try {
            SolicitacaoHardware solicitacao = solicitacaoService.criarSolicitacao(userDetails.getUsername(), dto);
            SolicitacaoResponse response = SolicitacaoMapper.toResponse(solicitacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
