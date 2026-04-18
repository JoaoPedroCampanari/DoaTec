package com.doatec.mapper;

import com.doatec.dto.request.PessoaUpdateRequest;
import com.doatec.dto.request.RegistroRequest;
import com.doatec.dto.response.UsuarioAdminResponse;
import com.doatec.dto.response.UserLoginResponse;
import com.doatec.model.account.Pessoa;
import com.doatec.model.account.Role;
import com.doatec.model.account.TipoPessoa;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PessoaMapper {

    public static Pessoa toPessoa(RegistroRequest request) {
        TipoPessoa tipoPessoa = TipoPessoa.valueOf(request.tipoPessoa().toUpperCase());

        return Pessoa.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(request.senha())
                .documento(request.documento())
                .tipoPessoa(tipoPessoa)
                .role(Role.USER) // Novo usuário sempre começa com role USER
                .endereco(request.endereco() != null ? request.endereco() : "")
                .telefone(request.telefone() != null ? request.telefone() : "")
                .ativo(true)
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
                pessoa.getTipoPessoa(),
                pessoa.getRole(),
                pessoa.getDocumento(),
                pessoa.getAtivo()
        );
    }

    public static UsuarioAdminResponse toAdminResponse(Pessoa pessoa) {
        return UsuarioAdminResponse.from(pessoa);
    }
}