package com.services.crm.models.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de sesión de caja para la API.
 *
 * @param id            identificador único de la sesión (String)
 * @param initialAmount fondo de caja inicial
 * @param totalSales    total acumulado de ventas de la sesión
 * @param cashTotal     total en efectivo recibido
 * @param cardTotal     total en tarjeta recibido
 * @param transferTotal total en transferencias recibido
 * @param openedAt      fecha/hora de apertura (ISO 8601)
 * @param closedAt      fecha/hora de cierre, null si sigue abierta
 */
public record CashSessionDTO(
        String id,
        BigDecimal initialAmount,
        BigDecimal totalSales,
        BigDecimal cashTotal,
        BigDecimal cardTotal,
        BigDecimal transferTotal,
        OffsetDateTime openedAt,
        OffsetDateTime closedAt) {
}
