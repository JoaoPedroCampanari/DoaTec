package com.doatec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SuporteFormularioRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo email é obrigatório")
    String email,

    @NotBlank(message = "O campo assunto é obrigatório")
    String assunto,

    @NotBlank(message = "O campo mensagem é obrigatório")
    String mensagem
) {}