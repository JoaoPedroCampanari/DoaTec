package com.doatec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.Builder;

@Builder
public record RegistroRequest(
    @NotBlank(message = "O campo tipo de pessoa é obrigatório")
    String tipoPessoa,

    @NotBlank(message = "O campo nome é obrigatório")
    String nome,

    @NotBlank(message = "O campo documento é obrigatório")
    String documento,

    @NotBlank(message = "O campo email é obrigatório")
    @Email(message = "O campo email deve ter um formato válido")
    String email,

    String endereco,

    String telefone,

    @NotBlank(message = "O campo senha é obrigatório")
    @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
    String senha
) {
    @AssertTrue(message = "O RA deve conter exatamente 13 digitos")
    public boolean isRaValido() {
        return !"ALUNO".equalsIgnoreCase(tipoPessoa) || (documento != null && documento.matches("[0-9]{13}"));
    }
}