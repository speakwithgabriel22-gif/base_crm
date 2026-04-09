package com.services.crm.models.response;

import java.math.BigDecimal;

/**
 * DTO de línea de venta para la API.
 *
 * @param id              identificador único del item (String)
 * @param upc             código de barras del producto vendido
 * @param productName     nombre del producto al momento de la venta
 * @param measurementUnit unidad de medida (PIECE, KG, etc.)
 * @param quantity        cantidad vendida (BigDecimal para soporte de peso)
 * @param unitPrice       precio unitario al momento de la venta
 * @param subtotal        subtotal de esta línea (quantity × unitPrice)
 */
public record SaleItemDTO(
    String id,
    String upc,
    String productName,
    String measurementUnit,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
