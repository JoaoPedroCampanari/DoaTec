package com.doatec.dto.request;

import com.doatec.validation.TipoDocumento;
import com.doatec.validation.ValidDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para registro de doadores pessoa jurídica (empresas).
 */
@Builder
public record DoadorPJRegistroRequest(
    @NotBlank(message = "O campo razão social é obrigatório")
    String razaoSocial,

    @NotBlank(message = "O campo nome do representante é obrigatório")
    String nomeRepresentante,

    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O campo email deve ter um formato válido")
    String email,

    @NotBlank(message = "O campo senha é obrigatório")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha,

    @NotBlank(message = "O campo CNPJ é obrigatório")
    @ValidDocumento(tipo = TipoDocumento.CNPJ)
    String cnpj,

    String telefone,

    String endereco
) {}