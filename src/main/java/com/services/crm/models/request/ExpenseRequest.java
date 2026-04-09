package com.services.crm.models.request;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.services.crm.enums.PaymentType;

/**
 * Request para registrar un gasto operativo.
 *
 * @param cashSessionId sesión de caja asociada
 * @param category      categoría (LUZ, AGUA, SUELDOS, etc.)
 * @param description   descripción detallada del gasto
 * @param amount        monto del gasto
 * @param note          nota adicional opcional
 * @param paymentMethod método de pago (CASH, CARD, TRANSFER)
 */
public record ExpenseRequest(
    @NotNull(message = "El ID de la sesión de caja es obligatorio")
    UUID cashSessionId,

    @NotBlank(message = "La categoría es obligatoria")
    String category,

    String description,

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal amount,

    String note,

    PaymentType paymentMethod // Opcional, por defecto CASH
) {}
