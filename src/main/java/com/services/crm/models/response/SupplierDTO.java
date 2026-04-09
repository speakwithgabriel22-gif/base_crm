package com.services.crm.models.response;

import java.math.BigDecimal;

/**
 * DTO de proveedor para la API.
 *
 * @param id          identificador único del proveedor (String)
 * @param name        nombre o razón social del proveedor
 * @param contactName nombre del contacto o preventista
 * @param phone       teléfono de contacto
 * @param category    categoría de productos que surte
 * @param visitDay    día(s) de visita del preventista
 * @param deliveryDay día(s) de entrega de mercancía
 * @param frequency   frecuencia de visita (Semanal, Quincenal, etc.)
 * @param totalDebt   deuda total pendiente con el proveedor
 */
public record SupplierDTO(
    String id,
    String name,
    String contactName,
    String phone,
    String category,
    String visitDay,
    String deliveryDay,
    String frequency,
    BigDecimal totalDebt
) {}
