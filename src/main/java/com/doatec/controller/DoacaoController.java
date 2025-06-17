package com.doatec.controller;

import com.doatec.dtos.DoacaoDto;
import com.doatec.model.donation.Doacao;
import com.doatec.service.DoacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @PostMapping
    public ResponseEntity<String> createDonation(@RequestBody DoacaoDto doacaoDto) {
        try {
            // Tenta registrar a doação.
            Doacao novaDoacao = doacaoService.registrarDoacao(doacaoDto);
            // Se der certo, retorna uma mensagem de sucesso.
            return ResponseEntity.status(HttpStatus.CREATED).body("Doação registrada com sucesso! ID: " + novaDoacao.getId());
        } catch (Exception e) {
            // Se acontecer um erro, retorna uma mensagem de erro.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}