package com.doatec.controller;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.mapper.SuporteMapper;
import com.doatec.model.suporte.SuporteFormulario;
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
    public ResponseEntity<SuporteResponse> receberFormulario(@Valid @RequestBody SuporteFormularioRequest dto) {
        try {
            SuporteFormulario ticket = suporteService.criarTicket(dto);
            SuporteResponse response = SuporteMapper.toResponse(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}