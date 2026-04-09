package com.services.crm.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no hay suficiente efectivo disponible en la caja.
 */
public class InsufficientCashException extends BusinessException {
    public InsufficientCashException(String message) {
        super("INSUFFICIENT_CASH", message, HttpStatus.BAD_REQUEST);
    }
}
