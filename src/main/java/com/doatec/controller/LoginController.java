package com.doatec.controller;

import com.doatec.dto.request.LoginRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.repository.PessoaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @PostMapping
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            String identificador = loginRequest.email().trim();
            if (!identificador.contains("@")) {
                // Prefixos antigos são aceitos para manter compatibilidade com clientes existentes.
                identificador = identificador.replaceFirst("(?i)^(CPF|CNPJ|RA):", "").trim();
                if (!identificador.matches("[0-9. /-]+"))
                    throw new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas");
                String documento = identificador.replaceAll("[^0-9]", "");
                String tipo = switch (documento.length()) {
                    case 11 -> "CPF";
                    case 13 -> "RA";
                    case 14 -> "CNPJ";
                    default -> throw new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas");
                };
                java.util.List<Integer> ids = pessoaRepository.buscarIdsPorDocumento(tipo, documento);
                if (ids.size() != 1) throw new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas");
                identificador = pessoaRepository.findById(ids.get(0))
                    .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas")).getEmail();
            }
            // Autentica usando Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identificador, loginRequest.senha())
            );

            // Cria um novo contexto de segurança e define a autenticação
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Persiste a sessão no servidor (envia Set-Cookie para o navegador)
            securityContextRepository.saveContext(context, request, response);

            // Busca dados completos do usuário
            Pessoa pessoa = pessoaRepository.findByEmail(identificador)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            UserLoginResponse responseDto = PessoaMapper.toResponse(pessoa);
            return ResponseEntity.ok(responseDto);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou senha incorretos.");
        }
    }
}