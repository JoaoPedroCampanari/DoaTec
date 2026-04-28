package com.doatec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarAdminRequest(
    @NotBlank String nome,
    @Email @NotBlank String email,
    @Size(min = 6) String senha,
    @NotBlank String tipoPessoa,
    @NotBlank String documento,
    @NotBlank String role
) {}
