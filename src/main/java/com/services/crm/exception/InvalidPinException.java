package com.services.crm.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando el PIN ingresado es incorrecto.
 */
public class InvalidPinException extends BusinessException {
    public InvalidPinException(String message) {
        super("INVALID_PIN", message, HttpStatus.UNAUTHORIZED);
    }
}
