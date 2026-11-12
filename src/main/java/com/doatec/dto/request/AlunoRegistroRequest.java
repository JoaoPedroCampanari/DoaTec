package com.doatec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para registro de novos alunos.
 * Alunos sao beneficiarios que solicitam equipamentos.
 */
@Builder
public record AlunoRegistroRequest(
    @NotBlank(message = "O campo nome e obrigatorio")
    String nome,

    @NotBlank(message = "O campo email e obrigatorio")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Insira um e-mail valido")
    String email,

    @NotBlank(message = "O campo senha e obrigatorio")
    @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
    String senha,

    @NotBlank(message = "O campo RA e obrigatorio")
    @Pattern(regexp = "[0-9]{13}", message = "O RA deve conter exatamente 13 digitos numericos")
    String ra,

    String telefone,

    String cep,
    String logradouro,
    String numero,
    String bairro,
    String cidade,
    String estado
) {
}