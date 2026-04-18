package com.doatec.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação customizada para RA (Registro de Aluno).
 * O RA deve conter apenas caracteres alfanuméricos e ter entre 4 e 20 caracteres.
 */
@Constraint(validatedBy = RAValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRA {
    String message() default "RA inválido. Deve conter entre 4 e 20 caracteres alfanuméricos.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 4;
    int max() default 20;
}