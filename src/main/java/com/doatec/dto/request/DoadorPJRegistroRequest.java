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
    @Size(max = 150, message = "A razão social deve ter no máximo 150 caracteres")
    String razaoSocial,

    @NotBlank(message = "O campo nome do representante e obrigatorio")
    @Size(max = 100, message = "O nome do representante deve ter no máximo 100 caracteres")
    String nomeRepresentante,

    @NotBlank(message = "O campo email e obrigatorio")
    @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Insira um e-mail valido")
    String email,

    @NotBlank(message = "O campo senha e obrigatorio")
    @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
    String senha,

    @NotBlank(message = "O campo CNPJ e obrigatorio")
    @ValidDocumento(tipo = TipoDocumento.CNPJ)
    String cnpj,

    String telefone,

    String cep,
    String logradouro,
    @Size(max = 10, message = "O número deve ter no máximo 10 caracteres")
    String numero,
    String bairro,
    String cidade,
    String estado
) {}
