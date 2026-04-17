package com.doatec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SolicitacaoRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo email é obrigatório")
    String email,

    @NotBlank(message = "O campo senha é obrigatório")
    String senha,

    String telefone,

    String ra,
    String justificativa,
    String preferenciaEquipamento
) {}