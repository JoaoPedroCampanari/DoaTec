package com.doatec.dto.request;

import lombok.Builder;
import jakarta.validation.constraints.Pattern;

@Builder
public record PessoaUpdateRequest(
    String email,
    String senha,
    @Pattern(regexp = "^(?!0{8}$)[0-9]{8}$", message = "Informe um CEP válido com 8 dígitos")
    String endereco, // CEP
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado,
    String telefone
) {}