package com.services.crm.models.response;

import java.time.OffsetDateTime;

/**
 * Record que representa la respuesta unificada de la API de MEYISOFT POS.
 *
 * @param ok          indica si la operación fue exitosa
 * @param result      los datos de respuesta en caso de éxito
 * @param errorCode   el código de error si ok es false
 * @param userMessage el mensaje descriptivo para el usuario
 * @param timestamp   el momento en el que se generó la respuesta
 * @param <T>         el tipo de los datos de respuesta
 */
public record ApiResponse<T>(
        boolean ok,
        T result,
        String errorCode,
        String userMessage,
        OffsetDateTime timestamp) {
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, result, null, null, OffsetDateTime.now());
    }

    public static <T> ApiResponse<T> error(String errorCode, String userMessage) {
        return new ApiResponse<>(false, null, errorCode, userMessage, OffsetDateTime.now());
    }
}
