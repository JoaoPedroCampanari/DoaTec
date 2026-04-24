package com.doatec.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AvaliacaoRequest(
    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres")
    String observacao
) {}
