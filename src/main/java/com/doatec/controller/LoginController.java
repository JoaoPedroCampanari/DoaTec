package com.doatec.controller;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PessoaRepository pessoaRepository;

    @PostMapping
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autentica usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.senha())
            );

            // Define a autenticação no contexto de segurança (cria sessão)
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Busca dados completos do usuário
            Pessoa pessoa = pessoaRepository.findByEmail(loginRequest.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            UserLoginResponse responseDto = PessoaMapper.toResponse(pessoa);
            return ResponseEntity.ok(responseDto);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}