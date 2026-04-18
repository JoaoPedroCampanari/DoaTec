package com.doatec.dto.response;

import com.doatec.model.account.Pessoa;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UsuarioAdminResponse(
    Integer id,
    String nome,
    String email,
    String documento,
    String telefone,
    String endereco,
    String tipoPessoa,
    String role,
    Boolean ativo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UsuarioAdminResponse from(Pessoa pessoa) {
        return UsuarioAdminResponse.builder()
                .id(pessoa.getId())
                .nome(pessoa.getNome())
                .email(pessoa.getEmail())
                .documento(pessoa.getDocumento())
                .telefone(pessoa.getTelefone())
                .endereco(pessoa.getEndereco())
                .tipoPessoa(pessoa.getTipoPessoa().name())
                .role(pessoa.getRole().name())
                .ativo(pessoa.getAtivo())
                .createdAt(pessoa.getCreatedAt())
                .updatedAt(pessoa.getUpdatedAt())
                .build();
    }
}