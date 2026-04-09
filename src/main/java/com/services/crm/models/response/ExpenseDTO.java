package com.services.crm.models.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.services.crm.enums.PaymentType;

/**
 * DTO de gasto para la API.
 *
 * @param id            identificador único del gasto (String)
 * @param cashSessionId sesión de caja asociada (String)
 * @param category      categoría del gasto (LUZ, AGUA, SUELDOS, etc.)
 * @param description   descripción detallada del gasto
 * @param amount        monto del gasto
 * @param note          nota adicional opcional
 * @param paymentMethod método de pago (CASH, CARD, TRANSFER)
 * @param createdAt     fecha de creación (ISO 8601)
 */
public record ExpenseDTO(
        String id,
        String cashSessionId,
        String category,
        String description,
        BigDecimal amount,
        String note,
        PaymentType paymentMethod,
        OffsetDateTime createdAt) {
}
