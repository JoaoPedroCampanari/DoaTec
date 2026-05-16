package com.doatec.validation;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("RAValidator")
class RAValidatorTest {

    private RAValidator validator;

    @BeforeEach
    void setUp() {
        validator = new RAValidator();
        // Simula os valores default da anotacao @ValidRA (min=4, max=20)
        ReflectionTestUtils.setField(validator, "min", 4);
        ReflectionTestUtils.setField(validator, "max", 20);
    }

    @Nested
    @DisplayName("Quando RA e nulo ou vazio")
    class NuloOuVazio {

        @Test
        @DisplayName("retorna true para null — @NotBlank cuida da validacao")
        void raNull_deveRetornarTrue() {
            assertTrue(validator.isValid(null, null));
        }

        @Test
        @DisplayName("retorna true para string vazia")
        void raVazio_deveRetornarTrue() {
            assertTrue(validator.isValid("", null));
        }

        @Test
        @DisplayName("retorna true para string com apenas espacos")
        void raSomenteEspacos_deveRetornarTrue() {
            assertTrue(validator.isValid("   ", null));
        }
    }

    @Nested
    @DisplayName("Quando RA e valido")
    class RaValido {

        @ParameterizedTest
        @DisplayName("aceita RAs alfanumericos dentro do tamanho")
        @ValueSource(strings = {
                "1234",           // minimo (4 chars)
                "ABC123",         // 6 chars
                "RA20240001",     // 10 chars
                "abcdefghijklmnopqrst", // maximo (20 chars)
                "AbCdEf1234",     // misto maiusculas e numeros
                "ra1234567890"    // 12 chars minusculas
        })
        void raValido_deveRetornarTrue(String ra) {
            assertTrue(validator.isValid(ra, null));
        }
    }

    @Nested
    @DisplayName("Quando RA e invalido")
    class RaInvalido {

        @Test
        @DisplayName("rejeita RA com menos de 4 caracteres")
        void raCurto_deveRetornarFalse() {
            assertFalse(validator.isValid("AB1", null));
        }

        @Test
        @DisplayName("rejeita RA com mais de 20 caracteres")
        void raLongo_deveRetornarFalse() {
            assertFalse(validator.isValid("A1B2C3D4E5F6G7H8I9J0K", null));
        }

        @ParameterizedTest
        @DisplayName("rejeita RA com caracteres especiais")
        @ValueSource(strings = {
                "RA-1234",        // hifen
                "RA_1234",        // underline
                "RA 1234",        // espaco no meio
                "RA@1234",        // arroba
                "RA#1234",        // cerquilha
                "RA.1234",        // ponto
                "RA,1234",        // virgula
                "1234!",          // exclamacao
                "1234$",          // cifrao
                "1234%",          // porcentagem
                "RA+1234",        // mais
                "RA=1234"         // igual
        })
        void raComCaractereEspecial_deveRetornarFalse(String ra) {
            assertFalse(validator.isValid(ra, null));
        }
    }

    @Nested
    @DisplayName("Quando min/max sao customizados")
    class MinMaxCustomizados {

        @Test
        @DisplayName("aceita RA com tamanho exatamente no minimo customizado")
        void raNoMinimoCustomizado_deveRetornarTrue() {
            ReflectionTestUtils.setField(validator, "min", 6);
            ReflectionTestUtils.setField(validator, "max", 10);

            assertTrue(validator.isValid("ABC123", null));
        }

        @Test
        @DisplayName("aceita RA com tamanho exatamente no maximo customizado")
        void raNoMaximoCustomizado_deveRetornarTrue() {
            ReflectionTestUtils.setField(validator, "min", 6);
            ReflectionTestUtils.setField(validator, "max", 10);

            assertTrue(validator.isValid("ABC1234567", null));
        }

        @Test
        @DisplayName("rejeita RA abaixo do minimo customizado")
        void raAbaixoDoMinimoCustomizado_deveRetornarFalse() {
            ReflectionTestUtils.setField(validator, "min", 6);
            ReflectionTestUtils.setField(validator, "max", 10);

            assertFalse(validator.isValid("ABC12", null));
        }

        @Test
        @DisplayName("rejeita RA acima do maximo customizado")
        void raAcimaDoMaximoCustomizado_deveRetornarFalse() {
            ReflectionTestUtils.setField(validator, "min", 6);
            ReflectionTestUtils.setField(validator, "max", 10);

            assertFalse(validator.isValid("ABC12345678", null));
        }
    }
}
