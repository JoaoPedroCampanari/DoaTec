package com.doatec.dto.request;

import com.doatec.model.chat.ContextoChat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    @NotBlank @Size(max = 500) String conteudo,
    @NotNull Integer referenciaId,
    @NotNull ContextoChat contexto
) {}
