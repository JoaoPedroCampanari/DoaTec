package com.doatec.dto.request;

import lombok.Builder;

@Builder
public record PessoaUpdateRequest(
    String email,
    String senha,
    String endereco,
    String telefone
) {}