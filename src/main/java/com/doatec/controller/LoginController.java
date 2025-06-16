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

/**
 * Controlador para lidar com a autenticação de usuários.
 */
@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<String> loginUser(@RequestBody LoginDto loginDto) {
        // Chama o serviço para verificar se o email e a senha são válidos.
        boolean credenciaisValidas = pessoaService.verificarCredenciais(loginDto);

        if (credenciaisValidas) {
            // Se as credenciais estiverem corretas, retorna uma resposta de sucesso.
            return ResponseEntity.ok("Login bem-sucedido! Redirecionando para a home.");
        } else {
            // Se estiverem incorretas, retorna uma resposta de não autorizado.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}