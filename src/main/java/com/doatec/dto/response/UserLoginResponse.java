package com.doatec.dto.response;

import com.doatec.model.account.Role;
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
    /**
     * Cria UserLoginResponse a partir dos dados da pessoa.
     * Aceita tipoPessoa como String (retorno do método polimórfico getTipoPessoa()).
     */
    public static UserLoginResponse from(Integer id, String nome, String email,
            String telefone, String tipoPessoa, Role role, String documento, Boolean ativo) {
        return UserLoginResponse.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .tipoPessoa(tipoPessoa)
                .role(role.name())
                .documento(documento)
                .ativo(ativo)
                .build();
    }
}