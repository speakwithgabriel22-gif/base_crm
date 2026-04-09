package com.services.crm.models.response;

import java.time.OffsetDateTime;

/**
 * Representa la asociación de un usuario a un tenant específico,
 * incluyendo su rol en esa sucursal.
 */
public record UserTenantDTO(
        TenantDTO tenant,
        String role,
        OffsetDateTime joinedAt) {
}
