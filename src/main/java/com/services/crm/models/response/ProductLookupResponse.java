package com.services.crm.models.response;

import java.math.BigDecimal;

/**
 * Respuesta de búsqueda rápida de producto (escáner/nombre).
 *
 * @param scenario      LOCAL (inventario), GLOBAL (catálogo), NOT_FOUND
 * @param upc           código de barras buscado
 * @param name          nombre del producto (null si NOT_FOUND)
 * @param price         precio actual (null si GLOBAL/NOT_FOUND)
 * @param stock         stock disponible (null si GLOBAL/NOT_FOUND)
 * @param requiresPrice true si el frontend debe pedir precio antes de vender
 */
public record ProductLookupResponse(
    String scenario,
    String upc,
    String name,
    BigDecimal price,
    BigDecimal stock,
    boolean requiresPrice
) {}
