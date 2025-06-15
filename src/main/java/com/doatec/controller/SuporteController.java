package com.doatec.controller;

import com.doatec.dtos.SuporteFormularioDto;
import com.doatec.service.SuporteFormularioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/suporte")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class SuporteController {

    @Autowired
    private SuporteFormularioService suporteService;

    @PostMapping
    public ResponseEntity<String> receberFormulario(@RequestBody SuporteFormularioDto dto) {
        try {
            suporteService.criarTicket(dto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Mensagem de suporte recebida! Responderemos em breve no email fornecido.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar sua solicitação de suporte.");
        }
    }
}
