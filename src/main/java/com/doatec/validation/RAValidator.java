package com.doatec.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador de RA (Registro de Aluno).
 * Aceita caracteres alfanuméricos com tamanho entre min e max.
 */
public class RAValidator implements ConstraintValidator<ValidRA, String> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidRA constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(String ra, ConstraintValidatorContext context) {
        if (ra == null || ra.isBlank()) {
            return true; // @NotBlank cuida de campos vazios
        }

        // Verifica tamanho
        if (ra.length() < min || ra.length() > max) {
            return false;
        }

        // Verifica se contém apenas caracteres alfanuméricos
        return ra.matches("^[a-zA-Z0-9]+$");
    }
}