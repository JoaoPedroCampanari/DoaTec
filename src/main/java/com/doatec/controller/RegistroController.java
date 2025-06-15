package com.doatec.controller;

import com.doatec.dtos.RegistroDto;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
public class RegistroController {

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<String> registerUser(@RequestBody RegistroDto registroDto) {
        try {

            Pessoa novaPessoa = pessoaService.registrarPessoa(registroDto);
            if (novaPessoa != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Usuário registrado com sucesso!");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erro ao registrar usuário. Tipo de usuário inválido.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao registrar usuário: " + e.getMessage());
        }
    }
}
