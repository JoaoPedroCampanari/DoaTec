package com.doatec.dto.request;

import com.doatec.validation.ValidRA;
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
    @Size(min = 6, message = "A senha deve ter no minimo 6 digitos")
    String senha,

    @NotBlank(message = "O campo RA e obrigatorio")
    @ValidRA
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