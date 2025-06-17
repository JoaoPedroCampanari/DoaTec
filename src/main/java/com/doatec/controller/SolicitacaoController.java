package com.doatec.controller;

import com.doatec.dtos.SolicitacaoDto;
import com.doatec.service.SolicitacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para gerenciar as solicitações de hardware dos alunos.
 */
@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoController {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @PostMapping
    public ResponseEntity<String> criar(@RequestBody SolicitacaoDto dto) {
        try {
            // Tenta criar a solicitação através do serviço.
            solicitacaoService.criarSolicitacao(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Solicitação registrada com sucesso! Nossa equipe analisará seu pedido e entrará em contato.");
        } catch (RuntimeException e) {
            // Se o serviço lançar um erro, retorna a mensagem de erro específica.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}