package com.doatec.controller;

import com.doatec.dtos.LoginDto;
import com.doatec.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<String> loginUser(@RequestBody LoginDto loginDto) {

        boolean credenciaisValidas = pessoaService.verificarCredenciais(loginDto);

        if (credenciaisValidas) {
            return ResponseEntity.ok("Login bem-sucedido! Redirecionando para a home.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}
