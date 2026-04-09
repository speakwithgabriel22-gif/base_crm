package com.services.crm.models.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de cierre de caja con cálculo de diferencia.
 * Extiende los datos de la sesión con expectedCash, actualCash y difference.
 *
 * @param id            identificador de la sesión (String)
 * @param initialAmount monto inicial del fondo de caja
 * @param totalSales    total acumulado de ventas
 * @param cashTotal     total en efectivo recibido
 * @param cardTotal     total en tarjeta recibido
 * @param transferTotal total en transferencias recibido
 * @param openedAt      fecha/hora de apertura
 * @param closedAt      fecha/hora de cierre
 * @param expectedCash  efectivo esperado calculado por el sistema
 * @param actualCash    efectivo real contado por el cajero
 * @param difference    diferencia (actualCash - expectedCash)
 */
public record CashCloseResponse(
        String id,
        BigDecimal initialAmount,
        BigDecimal totalSales,
        BigDecimal cashTotal,
        BigDecimal cardTotal,
        BigDecimal transferTotal,
        OffsetDateTime openedAt,
        OffsetDateTime closedAt,
        BigDecimal expectedCash,
        BigDecimal actualCash,
        BigDecimal difference) {
}
