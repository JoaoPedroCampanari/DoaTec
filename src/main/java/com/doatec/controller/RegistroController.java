package com.doatec.controller;

import com.doatec.dto.request.RegistroRequest;
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

/**
 * Controlador para o registro de novos usuários.
 * Tipos de pessoa: DOADOR_PF, DOADOR_PJ, ALUNO
 * Role padrão: USER (pode ser alterado posteriormente por ADMIN)
 */
@RestController
@RequestMapping("/api/register")
public class RegistroController {

    @Autowired
    private PessoaService pessoaService;

    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegistroRequest registroRequest) {
        try {
            Pessoa novaPessoa = pessoaService.registrarPessoa(registroRequest);

            // Retorna os dados do usuário registrado (sem a senha)
            UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno ao registrar usuário: " + e.getMessage());
        }
    }
}