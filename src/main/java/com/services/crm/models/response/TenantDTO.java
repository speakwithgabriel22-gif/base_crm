package com.services.crm.models.response;

import java.time.OffsetDateTime;

/**
 * DTO del tenant (tienda) para la API.
 *
 * @param id                 identificador único de la tienda (String)
 * @param name               nombre comercial de la tienda
 * @param email              correo de contacto
 * @param phone              teléfono de contacto
 * @param address            dirección física de la tienda
 * @param plan               plan actual: STARTER, PRO, ENTERPRISE
 * @param subscriptionStatus estado de la suscripción: TRIAL, ACTIVE, EXPIRED,
 *                           CANCELLED
 * @param trialEndsAt        fecha de fin de prueba gratuita (ISO 8601)
 */
public record TenantDTO(
        String id,
        String name,
        String email,
        String phone,
        String address,
        String plan,
        String subscriptionStatus,
        OffsetDateTime trialEndsAt) {
}
