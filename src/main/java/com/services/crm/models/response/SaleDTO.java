package com.services.crm.models.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO de venta (cabecera) para la API.
 *
 * @param id            identificador único de la venta (String)
 * @param cashSessionId sesión de caja donde se registró (String)
 * @param folio         folio único SAL-YYYYMMDD-XXXX
 * @param paymentType   método de pago (CASH, CARD, TRANSFER)
 * @param total         monto total de la venta
 * @param createdAt     fecha de creación (ISO 8601)
 * @param items         detalle de productos vendidos
 */
public record SaleDTO(
        String id,
        String cashSessionId,
        String folio,
        String paymentType,
        BigDecimal total,
        OffsetDateTime createdAt,
        List<SaleItemDTO> items) {
}
