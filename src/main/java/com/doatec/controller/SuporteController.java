package com.doatec.controller;

import com.doatec.dto.request.SuporteFormularioRequest;
import com.doatec.dto.response.SuporteResponse;
import com.doatec.mapper.SuporteMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.model.suporte.SuporteFormulario;
import com.doatec.repository.PessoaRepository;
import com.doatec.service.SuporteFormularioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/suporte")
public class SuporteController {

    @Autowired
    private SuporteFormularioService suporteService;

    @Autowired
    private PessoaRepository pessoaRepository;

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

    @GetMapping("/meus-tickets")
    public ResponseEntity<List<SuporteResponse>> getMeusTickets(@AuthenticationPrincipal User userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Pessoa autor = pessoaRepository.findByEmail(userDetails.getUsername())
                .orElse(null);
        if (autor == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<SuporteResponse> tickets = suporteService.getTicketsByAutorId(autor.getId());
        return ResponseEntity.ok(tickets);
    }
}