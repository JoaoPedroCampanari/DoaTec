package com.doatec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RegistroRequest(
    @NotBlank(message = "O campo tipo de pessoa é obrigatório")
    String tipoPessoa,

    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo documento é obrigatório")
    String documento,

    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O campo email deve ter um formato válido")
    String email,

    String endereco,

    String telefone,

    @NotBlank(message = "O campo senha é obrigatório")
    String senha
) {}