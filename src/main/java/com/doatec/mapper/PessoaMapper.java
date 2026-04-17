package com.doatec.mapper;

import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.TipoUsuario;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PessoaMapper {

    public static Pessoa toPessoa(RegistroRequest request) {
        return Pessoa.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .endereco(request.endereco() != null ? request.endereco() : "")
                .telefone("")
                .documento(request.identidade())
                .tipo(TipoUsuario.valueOf(request.tipoUsuario().toUpperCase()))
                .build();
    }

    public static void updatePessoaFromRequest(PessoaUpdateRequest request, Pessoa pessoa) {
        if (request.email() != null) pessoa.setEmail(request.email());
        if (request.senha() != null) pessoa.setSenha(request.senha());
        if (request.endereco() != null) pessoa.setEndereco(request.endereco());
        if (request.telefone() != null) pessoa.setTelefone(request.telefone());
    }

    public static UserLoginResponse toResponse(Pessoa pessoa) {
        return UserLoginResponse.from(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                pessoa.getTipo(),
                pessoa.getDocumento()
        );
    }
}