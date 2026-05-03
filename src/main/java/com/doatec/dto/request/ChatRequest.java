package com.doatec.dto.request;

import com.doatec.model.chat.ContextoChat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequest(
    @NotBlank String conteudo,
    @NotNull Integer referenciaId,
    @NotNull ContextoChat contexto
) {}
