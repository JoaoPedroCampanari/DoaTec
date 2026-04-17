package com.doatec.controller;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.service.SuporteFormularioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/suporte")
public class SuporteController {

    @Autowired
    private SuporteFormularioService suporteService;

    @PostMapping
    public ResponseEntity<String> receberFormulario(@Valid @RequestBody SuporteFormularioRequest dto) {
        try {
            suporteService.criarTicket(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Mensagem de suporte recebida! Responderemos em breve no email fornecido.");
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}