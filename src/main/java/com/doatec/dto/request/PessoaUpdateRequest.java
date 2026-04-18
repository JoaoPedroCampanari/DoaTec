package com.doatec.dto.request;

import lombok.Builder;

@Builder
public record PessoaUpdateRequest(
    String email,
    String senha,
    String endereco, // CEP
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado,
    String telefone
) {}