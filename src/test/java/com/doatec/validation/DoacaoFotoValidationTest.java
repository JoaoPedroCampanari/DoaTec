package com.doatec.validation;

import com.doatec.dto.request.DoacaoRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoacaoFotoValidationTest {
    @Test
    void exigeAoMenosUmaFoto() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertFalse(validator.validateValue(DoacaoRequest.class, "urlFoto", null).isEmpty());
            assertFalse(validator.validateValue(DoacaoRequest.class, "urlFoto", "  ").isEmpty());
            assertTrue(validator.validateValue(DoacaoRequest.class, "urlFoto", "/uploads/foto.jpg").isEmpty());
        }
    }
}
