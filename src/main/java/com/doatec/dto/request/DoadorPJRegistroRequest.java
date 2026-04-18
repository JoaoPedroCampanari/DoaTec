package com.doatec.dto.request;

import com.doatec.validation.TipoDocumento;
import com.doatec.validation.ValidDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para registro de doadores pessoa juridica (empresas).
 */
@Builder
public record DoadorPJRegistroRequest(
    @NotBlank(message = "O campo razao social e obrigatorio")
    String razaoSocial,

    @NotBlank(message = "O campo nome do representante e obrigatorio")
    String nomeRepresentante,

    @NotBlank(message = "O campo email e obrigatorio")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Insira um e-mail valido")
    String email,

    @NotBlank(message = "O campo senha e obrigatorio")
    @Size(min = 6, message = "A senha deve ter no minimo 6 digitos")
    String senha,

    @NotBlank(message = "O campo CNPJ e obrigatorio")
    @ValidDocumento(tipo = TipoDocumento.CNPJ)
    String cnpj,

    String telefone,

    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado
) {}