package com.doatec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record RespostaSuporteRequest(
    @NotBlank(message = "A resposta é obrigatória")
    String resposta
) {}