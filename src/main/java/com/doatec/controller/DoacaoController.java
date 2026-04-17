package com.doatec.controller;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.model.donation.Doacao;
import com.doatec.service.DoacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/donations")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @PostMapping
    public ResponseEntity<String> createDonation(@Valid @RequestBody DoacaoRequest doacaoRequest) {
        try {
            Doacao novaDoacao = doacaoService.registrarDoacao(doacaoRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("Doação registrada com sucesso! ID: " + novaDoacao.getId());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}