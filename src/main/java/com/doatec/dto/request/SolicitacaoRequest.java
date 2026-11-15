package com.doatec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SolicitacaoRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo email é obrigatório")
    String email,

    String telefone,

    String ra,
    @NotBlank(message = "Conte por que você precisa do equipamento")
    String justificativa,
    @NotBlank(message = "Informe qual equipamento você precisa")
    @jakarta.validation.constraints.Size(max = 100, message = "A preferência deve ter no máximo 100 caracteres")
    String preferenciaEquipamento,
    String urlComprovante
) {}
