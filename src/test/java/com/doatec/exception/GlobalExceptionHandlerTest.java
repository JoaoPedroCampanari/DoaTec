package com.doatec.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Set;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Nested
    @DisplayName("handleBusinessException")
    class BusinessExceptionTests {

        @Test
        @DisplayName("retorna 400 com mensagem da BusinessException")
        void retorna400ComMensagem() {
            BusinessException ex = new BusinessException("Saldo insuficiente");

            ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().status());
            assertEquals("Erro de Negócio", response.getBody().error());
            assertEquals("Saldo insuficiente", response.getBody().message());
            assertNull(response.getBody().details());
        }

        @Test
        @DisplayName("retorna ErrorResponse preenchido corretamente")
        void retornaErrorResponsePreenchido() {
            BusinessException ex = new BusinessException("Operação não permitida");

            ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex);
            ErrorResponse body = response.getBody();

            assertNotNull(body);
            assertNotNull(body.timestamp());
            assertEquals(400, body.status());
            assertEquals("Erro de Negócio", body.error());
            assertEquals("Operação não permitida", body.message());
        }
    }

    @Nested
    @DisplayName("handleValidation")
    class ValidationTests {

        @Test
        @DisplayName("retorna 400 com detalhes dos campos inválidos")
        void retorna400ComDetalhesDosCampos() {
            FieldError fieldError1 = new FieldError("pessoa", "email", "não pode estar vazio");
            FieldError fieldError2 = new FieldError("pessoa", "nome", "tamanho deve ser entre 3 e 100");

            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().status());
            assertEquals("Erro de Validação", response.getBody().error());
            assertEquals("Um ou mais campos estão inválidos", response.getBody().message());
            assertNotNull(response.getBody().details());
            assertEquals(2, response.getBody().details().size());
            assertTrue(response.getBody().details().contains("email: não pode estar vazio"));
            assertTrue(response.getBody().details().contains("nome: tamanho deve ser entre 3 e 100"));
        }

        @Test
        @DisplayName("retorna lista vazia de detalhes quando não há erros de campo")
        void retornaDetalhesVazioQuandoSemErros() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertNotNull(response.getBody().details());
            assertTrue(response.getBody().details().isEmpty());
        }
    }

    @Nested
    @DisplayName("handleGenericException")
    class GenericExceptionTests {

        @Test
        @DisplayName("retorna 500 sem expor detalhes internos")
        void retorna500SemDetalhes() {
            Exception ex = new Exception("Erro interno sensível");

            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(500, response.getBody().status());
            assertEquals("Erro Interno", response.getBody().error());
            assertEquals("Ocorreu um erro inesperado. Tente novamente mais tarde.", response.getBody().message());
            assertNull(response.getBody().details());
        }

        @Test
        @DisplayName("não expõe a mensagem original da exceção")
        void naoExpoeMensagemOriginal() {
            Exception ex = new Exception("NullPointerException em linha secreta");

            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertNotNull(response.getBody());
            assertNotEquals(ex.getMessage(), response.getBody().message());
            assertEquals("Ocorreu um erro inesperado. Tente novamente mais tarde.", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("handleConstraintViolation")
    class ConstraintViolationTests {

        @Test
        @DisplayName("retorna 400 com detalhes das violações")
        void retorna400ComViolacoes() {
            ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
            when(violation1.getMessage()).thenReturn("CPF inválido");

            ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
            when(violation2.getMessage()).thenReturn("RA inválido");

            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation1, violation2));

            ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().status());
            assertEquals("Erro de Validação", response.getBody().error());
            assertEquals("Documento inválido", response.getBody().message());
            assertNotNull(response.getBody().details());
            assertEquals(2, response.getBody().details().size());
        }
    }

    @Nested
    @DisplayName("handleEntityNotFound")
    class EntityNotFoundTests {

        @Test
        @DisplayName("retorna 404 com mensagem de entidade não encontrada")
        void retorna404ComMensagem() {
            EntityNotFoundException ex = new EntityNotFoundException("Pessoa não encontrada");

            ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(ex);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(404, response.getBody().status());
            assertEquals("Recurso Não Encontrado", response.getBody().error());
            assertEquals("Pessoa não encontrada", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("handleRuntimeException")
    class RuntimeExceptionTests {

        @Test
        @DisplayName("retorna 400 com mensagem da RuntimeException")
        void retorna400ComMensagem() {
            RuntimeException ex = new RuntimeException("Erro de validação");

            ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(400, response.getBody().status());
            assertEquals("Erro", response.getBody().error());
            assertEquals("Erro de validação", response.getBody().message());
        }
    }

    @Nested
    @DisplayName("handleDataIntegrity")
    class DataIntegrityTests {

        @Test
        @DisplayName("retorna 409 com mensagem de conflito de email")
        void retorna409ComConflitoEmail() {
            // Simular root cause com mensagem de constraint do PostgreSQL
            Exception rootCause = new Exception("ERROR: duplicate key value violates unique constraint \"uk_pessoa_email\"");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation", rootCause);

            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(409, response.getBody().status());
            assertEquals("Conflito de Dados", response.getBody().error());
            assertTrue(response.getBody().message().contains("Email"));
        }

        @Test
        @DisplayName("retorna 409 com mensagem de conflito de CPF")
        void retorna409ComConflitoCpf() {
            Exception rootCause = new Exception("ERROR: duplicate key value violates unique constraint \"uk_doador_pf_cpf\"");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation", rootCause);

            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().message().contains("CPF"));
        }

        @Test
        @DisplayName("retorna 409 com mensagem genérica quando não identifica constraint")
        void retorna409ComMensagemGenerica() {
            DataIntegrityViolationException ex = new DataIntegrityViolationException("erro genérico");

            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("Violação de integridade de dados", response.getBody().message());
        }
    }
}
