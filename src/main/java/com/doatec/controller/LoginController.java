package com.doatec.controller;

import com.doatec.dtos.LoginDto;
import com.doatec.dtos.UserLoginResponseDto; // Importar o novo DTO
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
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        Pessoa pessoaAutenticada = pessoaService.autenticar(loginDto);

        if (pessoaAutenticada != null) {
            // Cria o DTO de resposta com os dados da pessoa
            UserLoginResponseDto responseDto = new UserLoginResponseDto(
                    pessoaAutenticada.getId(),
                    pessoaAutenticada.getNome(),
                    pessoaAutenticada.getEmail(),
                    pessoaAutenticada.getTelefone(),
                    pessoaAutenticada.getTipo(),
                    pessoaAutenticada.getDocumento()
            );
            return ResponseEntity.ok(responseDto); // Retorna o DTO no corpo da resposta
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}