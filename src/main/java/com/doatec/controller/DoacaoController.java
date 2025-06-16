package com.doatec.controller;

import com.doatec.dtos.DoacaoDto;
import com.doatec.model.donation.Doacao;
import com.doatec.service.DoacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para gerenciar as operações de doação.
 */
@RestController
@RequestMapping("/api/donations")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @PostMapping
    public ResponseEntity<String> createDonation(@RequestBody DoacaoDto doacaoDto) {
        try {
            // Tenta registrar a doação através do serviço.
            Doacao novaDoacao = doacaoService.registrarDoacao(doacaoDto);
            // Se der certo, retorna o status 201 (CREATED) com uma mensagem de sucesso.
            return ResponseEntity.status(HttpStatus.CREATED).body("Doação registrada com sucesso! ID: " + novaDoacao.getId());
        } catch (Exception e) {
            // Se ocorrer um erro (ex: doador não encontrado), retorna o status 400 (BAD_REQUEST) com a mensagem de erro.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}