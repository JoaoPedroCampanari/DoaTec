package com.doatec.validation;

import com.doatec.dto.request.AlunoRegistroRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlunoRegistroRequestValidationTest {
    @Test
    void raDeveConterExatamenteTrezeDigitos() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            for (String ra : new String[]{"1190482321008", "0000000000000"}) {
                assertTrue(validator.validateValue(AlunoRegistroRequest.class, "ra", ra).isEmpty(), ra);
            }
            for (String ra : new String[]{"", "123456789012", "12345678901234", "123456789012A"}) {
                assertFalse(validator.validateValue(AlunoRegistroRequest.class, "ra", ra).isEmpty(), ra);
            }
            assertFalse(validator.validateValue(AlunoRegistroRequest.class, "ra", null).isEmpty());
        }
    }
}
