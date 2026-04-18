package com.doatec.dto.request;

import com.doatec.validation.ValidRA;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para registro de novos alunos.
 * Alunos são beneficiários que solicitam equipamentos.
 */
@Builder
public record AlunoRegistroRequest(
    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O campo email deve ter um formato válido")
    String email,

    @NotBlank(message = "O campo senha é obrigatório")
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    String senha,

    @NotBlank(message = "O campo RA é obrigatório")
    @ValidRA
    String ra,

    String telefone,

    String endereco
) {}