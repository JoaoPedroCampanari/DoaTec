package com.doatec.dto.request;

import com.doatec.validation.TipoDocumento;
import com.doatec.validation.ValidDocumento;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para registro de doadores pessoa fisica.
 */
@Builder
public record DoadorPFRegistroRequest(
    @NotBlank(message = "O campo nome e obrigatorio")
    String nome,

    @NotBlank(message = "O campo email e obrigatorio")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Insira um e-mail valido")
    String email,

    @NotBlank(message = "O campo senha e obrigatorio")
    @Size(min = 6, message = "A senha deve ter no minimo 6 digitos")
    String senha,

    @NotBlank(message = "O campo CPF e obrigatorio")
    @ValidDocumento(tipo = TipoDocumento.CPF)
    String cpf,

    String telefone,

    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado
) {}