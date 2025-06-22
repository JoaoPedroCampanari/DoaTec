package com.doatec.controller;

import com.doatec.dtos.DoacaoResponseDto;
import com.doatec.dtos.PessoaUpdateDto;
import com.doatec.dtos.UserLoginResponseDto;
import com.doatec.model.account.Pessoa;
import com.doatec.model.donation.Doacao;
import com.doatec.service.DoacaoService;
import com.doatec.service.PessoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private PessoaService pessoaService;

    @Autowired
    private DoacaoService doacaoService;

    @GetMapping("/{id}")
    public ResponseEntity<UserLoginResponseDto> getUserById(@PathVariable Integer id) {
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

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Integer id, @RequestBody PessoaUpdateDto dto) {
        try {
            Pessoa pessoaAtualizada = pessoaService.updatePessoaProfile(id, dto);
            return ResponseEntity.ok("Perfil atualizado com sucesso para " + pessoaAtualizada.getEmail());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao atualizar perfil: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/donations")
    public ResponseEntity<List<DoacaoResponseDto>> getUserDonations(@PathVariable Integer id) {
        List<Doacao> doacoes = doacaoService.findDoacoesByDoadorId(id);
        List<DoacaoResponseDto> responseDtos = doacoes.stream()
                .map(DoacaoResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }
}