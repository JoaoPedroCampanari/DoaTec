package com.doatec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginRequest(
    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O campo email deve ter um formato válido")
    String email,

    @NotBlank(message = "O campo senha é obrigatório")
    String senha
) {}