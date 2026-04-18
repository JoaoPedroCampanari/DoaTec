package com.doatec.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handler global para tratamento de exceções da API.
 * Padroniza as respostas de erro em formato JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata erros de validação do Bean Validation (@Valid).
     * Retorna HTTP 400 com lista de campos com erro.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                "Um ou mais campos estão inválidos",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata erros de validação de constraints (ex: @ValidDocumento).
     * Retorna HTTP 400 com detalhes da violação.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Validação",
                "Documento inválido",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata erros de regras de negócio.
     * Retorna HTTP 400 com mensagem específica.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de Negócio",
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata entidade não encontrada.
     * Retorna HTTP 404.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Recurso Não Encontrado",
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Trata violação de integridade de dados (ex: email/CPF duplicado).
     * Retorna HTTP 409 Conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Violação de integridade de dados";

        // Tenta extrair mensagem mais específica
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("email")) {
                message = "Email já cadastrado no sistema";
            } else if (ex.getMessage().contains("cpf")) {
                message = "CPF já cadastrado no sistema";
            } else if (ex.getMessage().contains("cnpj")) {
                message = "CNPJ já cadastrado no sistema";
            } else if (ex.getMessage().contains("ra")) {
                message = "RA já cadastrado no sistema";
            } else if (ex.getMessage().contains("documento")) {
                message = "Documento já cadastrado no sistema";
            }
        }

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflito de Dados",
                message
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Trata RuntimeException genéricas.
     * Retorna HTTP 400.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro",
                ex.getMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata exceções não esperadas.
     * Retorna HTTP 500 sem expor detalhes internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro Interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}