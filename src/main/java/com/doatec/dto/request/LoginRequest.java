package com.doatec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
    @NotBlank(message = "Informe seu e-mail ou documento")
    String email,

    @NotBlank(message = "O campo senha é obrigatório")
    String senha
) {}