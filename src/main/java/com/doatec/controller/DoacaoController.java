package com.doatec.controller;

import com.doatec.dto.request.DoacaoRequest;
import com.doatec.dto.response.DoacaoResponse;
import com.doatec.mapper.DoacaoMapper;
import com.doatec.model.donation.Doacao;
import com.doatec.model.donation.StatusDoacao;
import com.doatec.service.DoacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DoacaoController {

    @Autowired
    private DoacaoService doacaoService;

    @GetMapping
    public ResponseEntity<List<DoacaoResponse>> listDonations(
            @RequestParam(required = false) StatusDoacao status,
            @RequestParam(required = false) Integer doadorId) {
        List<DoacaoResponse> doacoes = doacaoService.listDoacoes(status, doadorId);
        return ResponseEntity.ok(doacoes);
    }

    @PostMapping
    public ResponseEntity<DoacaoResponse> createDonation(@Valid @RequestBody DoacaoRequest doacaoRequest) {
        try {
            Doacao novaDoacao = doacaoService.registrarDoacao(doacaoRequest);
            DoacaoResponse response = DoacaoMapper.toResponse(novaDoacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}