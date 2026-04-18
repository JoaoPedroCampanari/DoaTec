package com.doatec.dto.response;

import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import lombok.Builder;

@Builder
public record UserLoginResponse(
    Integer id,
    String nome,
    String email,
    String telefone,
    String tipoPessoa,
    String role,
    String documento,
    Boolean ativo
) {
    public static UserLoginResponse from(Integer id, String nome, String email,
            String telefone, TipoPessoa tipoPessoa, Role role, String documento, Boolean ativo) {
        return UserLoginResponse.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .tipoPessoa(tipoPessoa.name())
                .role(role.name())
                .documento(documento)
                .ativo(ativo)
                .build();
    }
}