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
    Boolean ativo,
    // Campos de endereco
    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado
) {
    /**
     * Cria UserLoginResponse a partir dos dados da pessoa.
     * Aceita tipoPessoa como String (retorno do metodo polimorfico getTipoPessoa()).
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

    /**
     * Cria UserLoginResponse completo com endereco.
     */
    public static UserLoginResponse fromWithAddress(Integer id, String nome, String email,
            String telefone, String tipoPessoa, Role role, String documento, Boolean ativo,
            String cep, String logradouro, String numero, String bairro, String cidade, String estado) {
        return UserLoginResponse.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .tipoPessoa(tipoPessoa)
                .role(role.name())
                .documento(documento)
                .ativo(ativo)
                .cep(cep)
                .logradouro(logradouro)
                .numero(numero)
                .bairro(bairro)
                .cidade(cidade)
                .estado(estado)
                .build();
    }
}