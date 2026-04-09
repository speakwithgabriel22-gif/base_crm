package com.services.crm.models.response;

import java.math.BigDecimal;

/**
 * DTO de producto en inventario para la API.
 *
 * @param id          identificador único del producto en la tienda (String)
 * @param upc         código de barras del producto
 * @param name        nombre comercial del producto
 * @param description descripción del producto
 * @param price       precio de venta actual
 * @param stock       cantidad disponible en inventario
 * @param minStock    cantidad mínima de alerta de reabastecimiento
 * @param category    categoría del producto
 * @param isActive    indica si el producto está activo en la tienda
 * @param status      estado de stock: CRITICO, BAJO, OK
 */
public record ProductDTO(
    String id,
    String upc,
    String name,
    String description,
    BigDecimal price,
    BigDecimal stock,
    BigDecimal minStock,
    String category,
    boolean isActive,
    String status
) {}
