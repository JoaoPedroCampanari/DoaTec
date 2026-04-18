package com.doatec.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação de validação customizada para documentos (CPF/CNPJ).
 * Valida os dígitos verificadores conforme algoritmos oficiais.
 */
@Constraint(validatedBy = DocumentoValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDocumento {
    String message() default "Documento inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * Tipo de documento a ser validado.
     * Pode ser CPF, CNPJ ou AMBOS (aceita qualquer um dos dois).
     */
    TipoDocumento tipo() default TipoDocumento.AMBOS;
}