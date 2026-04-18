package com.doatec.exception;

/**
 * Exceção para erros de regras de negócio.
 * Será tratada pelo GlobalExceptionHandler retornando HTTP 400.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}