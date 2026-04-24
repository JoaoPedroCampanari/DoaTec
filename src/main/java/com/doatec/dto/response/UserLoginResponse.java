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
    String estado,
    // Estatísticas do usuário
    Long totalDoacoes,
    Long totalSolicitacoes,
    Long totalTicketsSuporte
) {
    /**
     * Cria UserLoginResponse a partir dos dados da pessoa.
     * Aceita tipoPessoa como String (retorno do metodo polimorfico getTipoPessoa()).
     */
    public static UserLoginResponse from(Integer id, String nome, String email,
            String telefone, String tipoPessoa, Role role, String documento, Boolean ativo,
            Long totalDoacoes, Long totalSolicitacoes, Long totalTicketsSuporte) {
        return UserLoginResponse.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .tipoPessoa(tipoPessoa)
                .role(role.name())
                .documento(documento)
                .ativo(ativo)
                .totalDoacoes(totalDoacoes)
                .totalSolicitacoes(totalSolicitacoes)
                .totalTicketsSuporte(totalTicketsSuporte)
                .build();
    }

    /**
     * Cria UserLoginResponse completo com endereco.
     */
    public static UserLoginResponse fromWithAddress(Integer id, String nome, String email,
            String telefone, String tipoPessoa, Role role, String documento, Boolean ativo,
            String cep, String logradouro, String numero, String bairro, String cidade, String estado,
            Long totalDoacoes, Long totalSolicitacoes, Long totalTicketsSuporte) {
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
                .totalDoacoes(totalDoacoes)
                .totalSolicitacoes(totalSolicitacoes)
                .totalTicketsSuporte(totalTicketsSuporte)
                .build();
    }
}