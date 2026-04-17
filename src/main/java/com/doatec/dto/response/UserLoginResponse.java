package com.doatec.dto.response;

import com.doatec.model.account.TipoUsuario;
import lombok.Builder;

@Builder
public record UserLoginResponse(
    Integer id,
    String nome,
    String email,
    String telefone,
    String tipoUsuario,
    String documento
) {
    public static UserLoginResponse from(Integer id, String nome, String email,
            String telefone, TipoUsuario tipoUsuario, String documento) {
        return UserLoginResponse.builder()
                .id(id)
                .nome(nome)
                .email(email)
                .telefone(telefone)
                .tipoUsuario(tipoUsuario.name())
                .documento(documento)
                .build();
    }
}