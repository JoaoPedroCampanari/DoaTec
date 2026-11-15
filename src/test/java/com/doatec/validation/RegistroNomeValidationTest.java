package com.doatec.validation;

import com.doatec.dto.request.AlunoRegistroRequest;
import com.doatec.dto.request.DoadorPFRegistroRequest;
import com.doatec.dto.request.DoadorPJRegistroRequest;
import com.doatec.dto.request.RegistroRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistroNomeValidationTest {
    @Test
    void validaLimiteDoNumeroDaResidencia() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (var tipo : new Class<?>[]{AlunoRegistroRequest.class, DoadorPFRegistroRequest.class, DoadorPJRegistroRequest.class}) {
                assertTrue(validator.validateValue(tipo, "numero", "1234567890").isEmpty());
                assertFalse(validator.validateValue(tipo, "numero", "12345678901").isEmpty());
            }
        }
    }

    @Test
    void rejeitaEmailAcimaDoLimiteDoCadastro() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (var tipo : new Class<?>[]{AlunoRegistroRequest.class, DoadorPFRegistroRequest.class, DoadorPJRegistroRequest.class, RegistroRequest.class}) {
                String emailNoLimite = "a".repeat(114) + "@a.com";
                String emailAcimaDoLimite = "a" + emailNoLimite;
                assertTrue(validator.validateValue(tipo, "email", emailNoLimite).stream()
                        .noneMatch(v -> v.getMessage().contains("120 caracteres")));
                assertTrue(validator.validateValue(tipo, "email", emailAcimaDoLimite).stream()
                        .anyMatch(v -> v.getMessage().contains("120 caracteres")));
            }
        }
    }

    @Test
    void validaLimitesDeNomes() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            for (var tipo : new Class<?>[]{AlunoRegistroRequest.class, DoadorPFRegistroRequest.class, RegistroRequest.class}) {
                assertTrue(validator.validateValue(tipo, "nome", "a".repeat(100)).isEmpty());
                assertFalse(validator.validateValue(tipo, "nome", "a".repeat(101)).isEmpty());
            }
            assertTrue(validator.validateValue(DoadorPJRegistroRequest.class, "nomeRepresentante", "a".repeat(100)).isEmpty());
            assertFalse(validator.validateValue(DoadorPJRegistroRequest.class, "nomeRepresentante", "a".repeat(101)).isEmpty());
            assertTrue(validator.validateValue(DoadorPJRegistroRequest.class, "razaoSocial", "a".repeat(150)).isEmpty());
            assertFalse(validator.validateValue(DoadorPJRegistroRequest.class, "razaoSocial", "a".repeat(151)).isEmpty());
        }
    }
}
