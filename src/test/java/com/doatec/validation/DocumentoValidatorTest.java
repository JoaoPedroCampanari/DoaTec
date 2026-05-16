package com.doatec.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoValidator")
class DocumentoValidatorTest {

    private DocumentoValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new DocumentoValidator();
    }

    // ----------------------------------------------------------------
    // Helper: inicializa o validator com o TipoDocumento desejado
    // ----------------------------------------------------------------
    private void inicializarCom(TipoDocumento tipo) {
        ValidDocumento annotation = org.mockito.Mockito.mock(ValidDocumento.class);
        when(annotation.tipo()).thenReturn(tipo);
        validator.initialize(annotation);
    }

    // ================================================================
    // CPF
    // ================================================================
    @Nested
    @DisplayName("Validacao de CPF")
    class CpfTests {

        @BeforeEach
        void configurarCpf() {
            inicializarCom(TipoDocumento.CPF);
        }

        @Test
        @DisplayName("deve aceitar CPF valido com formatacao")
        void cpfValidoComFormatacao() {
            assertTrue(validator.isValid("529.982.247-25", context));
        }

        @Test
        @DisplayName("deve aceitar CPF valido sem formatacao")
        void cpfValidoSemFormatacao() {
            assertTrue(validator.isValid("52998224725", context));
        }

        @Test
        @DisplayName("deve rejeitar CPF com digitos verificadores errados")
        void cpfDigitosVerificadoresErrados() {
            assertFalse(validator.isValid("529.982.247-26", context));
        }

        @Test
        @DisplayName("deve rejeitar CPF com todos os digitos iguais")
        void cpfTodosDigitosIguais() {
            assertFalse(validator.isValid("111.111.111-11", context));
        }

        @Test
        @DisplayName("deve rejeitar CPF com tamanho incorreto")
        void cpfTamanhoIncorreto() {
            assertFalse(validator.isValid("1234567890", context));
        }

        @Test
        @DisplayName("deve rejeitar CPF com letras")
        void cpfComLetras() {
            assertFalse(validator.isValid("abc.def.ghi-jk", context));
        }

        @Test
        @DisplayName("deve aceitar CPF nulo (null)")
        void cpfNulo() {
            assertTrue(validator.isValid(null, context));
        }

        @Test
        @DisplayName("deve aceitar CPF vazio")
        void cpfVazio() {
            assertTrue(validator.isValid("", context));
        }

        @Test
        @DisplayName("deve aceitar CPF em branco")
        void cpfEmBranco() {
            assertTrue(validator.isValid("   ", context));
        }

        @Test
        @DisplayName("deve rejeitar CNPJ informado como CPF")
        void cnpjInformadoComoCpf() {
            assertFalse(validator.isValid("11.222.333/0001-81", context));
        }
    }

    // ================================================================
    // CNPJ
    // ================================================================
    @Nested
    @DisplayName("Validacao de CNPJ")
    class CnpjTests {

        @BeforeEach
        void configurarCnpj() {
            inicializarCom(TipoDocumento.CNPJ);
        }

        @Test
        @DisplayName("deve aceitar CNPJ valido com formatacao")
        void cnpjValidoComFormatacao() {
            assertTrue(validator.isValid("11.222.333/0001-81", context));
        }

        @Test
        @DisplayName("deve aceitar CNPJ valido sem formatacao")
        void cnpjValidoSemFormatacao() {
            assertTrue(validator.isValid("11222333000181", context));
        }

        @Test
        @DisplayName("deve rejeitar CNPJ com digitos verificadores errados")
        void cnpjDigitosVerificadoresErrados() {
            assertFalse(validator.isValid("11.222.333/0001-82", context));
        }

        @Test
        @DisplayName("deve rejeitar CNPJ com todos os digitos iguais")
        void cnpjTodosDigitosIguais() {
            assertFalse(validator.isValid("11.111.111/1111-11", context));
        }

        @Test
        @DisplayName("deve rejeitar CNPJ com tamanho incorreto")
        void cnpjTamanhoIncorreto() {
            assertFalse(validator.isValid("1234567890123", context));
        }

        @Test
        @DisplayName("deve rejeitar CNPJ com letras")
        void cnpjComLetras() {
            assertFalse(validator.isValid("ab.cde.fgh/ijkl-mn", context));
        }

        @Test
        @DisplayName("deve aceitar CNPJ nulo (null)")
        void cnpjNulo() {
            assertTrue(validator.isValid(null, context));
        }

        @Test
        @DisplayName("deve aceitar CNPJ vazio")
        void cnpjVazio() {
            assertTrue(validator.isValid("", context));
        }

        @Test
        @DisplayName("deve aceitar CNPJ em branco")
        void cnpjEmBranco() {
            assertTrue(validator.isValid("   ", context));
        }

        @Test
        @DisplayName("deve rejeitar CPF informado como CNPJ")
        void cpfInformadoComoCnpj() {
            assertFalse(validator.isValid("529.982.247-25", context));
        }
    }

    // ================================================================
    // AMBOS
    // ================================================================
    @Nested
    @DisplayName("Validacao de AMBOS (CPF ou CNPJ)")
    class AmbosTests {

        @BeforeEach
        void configurarAmbos() {
            inicializarCom(TipoDocumento.AMBOS);
        }

        @Test
        @DisplayName("deve aceitar CPF valido no modo AMBOS")
        void cpfValidoNoModoAmbos() {
            assertTrue(validator.isValid("529.982.247-25", context));
        }

        @Test
        @DisplayName("deve aceitar CNPJ valido no modo AMBOS")
        void cnpjValidoNoModoAmbos() {
            assertTrue(validator.isValid("11.222.333/0001-81", context));
        }

        @Test
        @DisplayName("deve rejeitar documento invalido no modo AMBOS")
        void documentoInvalidoNoModoAmbos() {
            assertFalse(validator.isValid("000.000.000-00", context));
        }

        @Test
        @DisplayName("deve aceitar nulo no modo AMBOS")
        void nuloNoModoAmbos() {
            assertTrue(validator.isValid(null, context));
        }
    }
}
