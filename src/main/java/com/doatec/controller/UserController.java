package com.doatec.controller;

import com.doatec.dtos.UserLoginResponseDto;
import com.doatec.model.account.Pessoa;
import com.doatec.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private PessoaService pessoaService;

    // Endpoint para buscar os dados do usuário logado (usando o ID armazenado no cliente)
    @GetMapping("/{id}")
    public ResponseEntity<UserLoginResponseDto> getUserById(@PathVariable Integer id) { // Alterado de String para Integer
        Pessoa pessoa = pessoaService.findById(id);
        if (pessoa != null) {
            UserLoginResponseDto responseDto = new UserLoginResponseDto(
                    pessoa.getId(),
                    pessoa.getNome(),
                    pessoa.getEmail(),
                    pessoa.getTelefone(),
                    pessoa.getTipo(),
                    pessoa.getDocumento()
            );
            return ResponseEntity.ok(responseDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}