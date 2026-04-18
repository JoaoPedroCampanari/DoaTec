package com.doatec.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de documentos (CPF e CNPJ).
 * Implementa os algoritmos de validação de dígitos verificadores.
 */
public class DocumentoValidator implements ConstraintValidator<ValidDocumento, String> {

    private TipoDocumento tipoDocumento;

    @Override
    public void initialize(ValidDocumento constraintAnnotation) {
        this.tipoDocumento = constraintAnnotation.tipo();
    }

    @Override
    public boolean isValid(String documento, ConstraintValidatorContext context) {
        if (documento == null || documento.isBlank()) {
            return true; // @NotBlank cuida de campos vazios
        }

        // Remove caracteres não numéricos
        String documentoLimpo = documento.replaceAll("[^0-9]", "");

        return switch (tipoDocumento) {
            case CPF -> validarCPF(documentoLimpo);
            case CNPJ -> validarCNPJ(documentoLimpo);
            case AMBOS -> validarCPF(documentoLimpo) || validarCNPJ(documentoLimpo);
        };
    }

    /**
     * Valida CPF (11 dígitos com 2 dígitos verificadores).
     */
    private boolean validarCPF(String cpf) {
        if (cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CPF inválido)
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        // Valida primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += (cpf.charAt(i) - '0') * (10 - i);
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;
        if (digito1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        // Valida segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += (cpf.charAt(i) - '0') * (11 - i);
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;
        return digito2 == (cpf.charAt(10) - '0');
    }

    /**
     * Valida CNPJ (14 dígitos com 2 dígitos verificadores).
     */
    private boolean validarCNPJ(String cnpj) {
        if (cnpj.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (CNPJ inválido)
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }

        // Valida primeiro dígito verificador
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos1[i];
        }
        int resto = soma % 11;
        int digito1 = (resto < 2) ? 0 : 11 - resto;
        if (digito1 != (cnpj.charAt(12) - '0')) {
            return false;
        }

        // Valida segundo dígito verificador
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += (cnpj.charAt(i) - '0') * pesos2[i];
        }
        resto = soma % 11;
        int digito2 = (resto < 2) ? 0 : 11 - resto;
        return digito2 == (cnpj.charAt(13) - '0');
    }
}