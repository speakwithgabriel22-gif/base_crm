package com.services.crm.models.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SaleRequest(
    @NotNull(message = "El ID de la sesión de caja es obligatorio")
    UUID cashSessionId,

    @NotBlank(message = "El tipo de pago es obligatorio")
    String paymentType, // CASH, CARD, TRANSFER

    @NotEmpty(message = "La venta debe tener al menos un producto")
    List<SaleItemRequest> items
) {}
