package com.doatec.controller;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        Pessoa pessoaAutenticada = pessoaService.autenticar(loginRequest);

        if (pessoaAutenticada != null) {
            UserLoginResponse responseDto = PessoaMapper.toResponse(pessoaAutenticada);
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}