package com.doatec.controller;

import com.doatec.dto.request.SolicitacaoRequest;
import com.doatec.service.SolicitacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> criar(@Valid @RequestBody SolicitacaoRequest dto) {
        try {
            solicitacaoService.criarSolicitacao(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Solicitação registrada com sucesso! Nossa equipe analisará seu pedido e entrará em contato.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}