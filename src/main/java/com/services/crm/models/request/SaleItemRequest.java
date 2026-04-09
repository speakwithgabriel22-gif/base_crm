package com.services.crm.models.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request de item de venta — identifica producto por UPC (escáner).
 *
 * @param upc       código de barras del producto a vender
 * @param quantity  cantidad (BigDecimal para soporte de peso)
 * @param unitPrice precio unitario al momento de la venta
 */
public record SaleItemRequest(
    @NotBlank(message = "El código de barras es obligatorio")
    String upc,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    BigDecimal quantity,

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    BigDecimal unitPrice
) {}
