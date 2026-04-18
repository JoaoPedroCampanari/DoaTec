package com.doatec.controller;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.mapper.PessoaMapper;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para o registro de novos usuários.
 *
 * Endpoints especializados:
 * - POST /api/register/aluno - Registro de alunos
 * - POST /api/register/doador-pf - Registro de doadores pessoa física
 * - POST /api/register/doador-pj - Registro de doadores pessoa jurídica
 *
 * Endpoint legado (mantido para compatibilidade):
 * - POST /api/register - Usa RegistroRequest genérico
 */
@RestController
@RequestMapping("/api/register")
public class RegistroController {

    @Autowired
    private PessoaService pessoaService;

    /**
     * Registro de aluno (beneficiário).
     * Alunos podem solicitar equipamentos disponíveis.
     */
    @PostMapping("/aluno")
    public ResponseEntity<UserLoginResponse> registrarAluno(@Valid @RequestBody AlunoRegistroRequest request) {
        Pessoa novaPessoa = pessoaService.registrarAluno(request);
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registro de doador pessoa física.
     * Doadores podem registrar doações de equipamentos.
     */
    @PostMapping("/doador-pf")
    public ResponseEntity<UserLoginResponse> registrarDoadorPF(@Valid @RequestBody DoadorPFRegistroRequest request) {
        Pessoa novaPessoa = pessoaService.registrarDoadorPF(request);
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Registro de doador pessoa jurídica (empresas).
     * Doadores podem registrar doações de equipamentos.
     */
    @PostMapping("/doador-pj")
    public ResponseEntity<UserLoginResponse> registrarDoadorPJ(@Valid @RequestBody DoadorPJRegistroRequest request) {
        Pessoa novaPessoa = pessoaService.registrarDoadorPJ(request);
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint legado para compatibilidade.
     * Usa RegistroRequest genérico (será descontinuado futuramente).
     */
    @PostMapping
    public ResponseEntity<UserLoginResponse> registerUser(@Valid @RequestBody RegistroRequest registroRequest) {
        Pessoa novaPessoa = pessoaService.registrarPessoa(registroRequest);
        UserLoginResponse response = PessoaMapper.toResponse(novaPessoa);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}