package com.doatec.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
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
     * Trata parâmetros de requisição inválidos (ex: enum inexistente, tipo incorreto).
     * Retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String param = ex.getName();
        String value = ex.getValue() != null ? ex.getValue().toString() : "";
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "";
        String message = "Valor inválido para o parâmetro '" + param + "': '" + value + "'. Tipo esperado: " + requiredType;

        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Parâmetro Inválido",
                message
        );

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Trata violação de integridade de dados (ex: email/CPF duplicado).
     * Retorna HTTP 409 Conflict.
     * Usa extração do nome da constraint do root cause para evitar
     * classificação incorreta por string-matching (ex: "ra" em "parameters").
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = "Violação de integridade de dados";

        // Extrai a constraint do root cause (PostgreSQL)
        String constraintName = extractConstraintName(ex);

        if (constraintName != null) {
            // Mapeia nomes de constraint para mensagens amigáveis
            String lower = constraintName.toLowerCase();
            if (lower.contains("email")) {
                message = "Email já cadastrado no sistema";
            } else if (lower.contains("cpf")) {
                message = "CPF já cadastrado no sistema";
            } else if (lower.contains("cnpj")) {
                message = "CNPJ já cadastrado no sistema";
            } else if (lower.equals("ra") || lower.endsWith("_ra") || lower.startsWith("ra_") || lower.contains("uk_ra") || lower.contains("un_ra") || lower.contains("unique_ra")) {
                message = "RA já cadastrado no sistema";
            } else if (lower.contains("documento")) {
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
     * Extrai o nome da constraint do root cause de uma DataIntegrityViolationException.
     * No PostgreSQL, o nome da constraint aparece na mensagem do PSQLException:
     * "ERROR: duplicate key value violates unique constraint \"uk_pessoa_email\""
     * ou "ERROR: null value in column \"ra\" violates not-null constraint"
     */
    private String extractConstraintName(DataIntegrityViolationException ex) {
        Throwable cause = ex.getRootCause();
        if (cause == null) cause = ex.getCause();
        while (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null) {
                // Log do root cause completo para diagnóstico
                org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
                        .error("DataIntegrityViolation root cause: {}", causeMessage);

                // PostgreSQL: duplicate key → "constraint \"name\""
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("constraint \"([^\"]+)\"").matcher(causeMessage);
                if (matcher.find()) {
                    return matcher.group(1);
                }
                // Fallback: column name → "column \"name\""
                matcher = java.util.regex.Pattern.compile("column \"([^\"]+)\"").matcher(causeMessage);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            cause = cause.getCause();
        }
        return null;
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