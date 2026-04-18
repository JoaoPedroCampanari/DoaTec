package com.doatec.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StatusUsuarioRequest(
    @NotNull(message = "O status ativo é obrigatório")
    Boolean ativo
) {}