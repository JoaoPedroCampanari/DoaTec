package com.doatec.validation;

import com.doatec.dto.request.SolicitacaoRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolicitacaoCamposValidationTest {
    @Test
    void exigeJustificativaEEquipamento() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (String campo : new String[]{"justificativa", "preferenciaEquipamento"}) {
                assertFalse(validator.validateValue(SolicitacaoRequest.class, campo, null).isEmpty());
                assertFalse(validator.validateValue(SolicitacaoRequest.class, campo, "   ").isEmpty());
                assertTrue(validator.validateValue(SolicitacaoRequest.class, campo, "Notebook para estudar").isEmpty());
            }
        }
    }
    @Test
    void limitaPreferenciaA100Caracteres() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertTrue(validator.validateValue(SolicitacaoRequest.class, "preferenciaEquipamento", "a".repeat(100)).isEmpty());
            assertFalse(validator.validateValue(SolicitacaoRequest.class, "preferenciaEquipamento", "a".repeat(101)).isEmpty());
        }
    }
}
