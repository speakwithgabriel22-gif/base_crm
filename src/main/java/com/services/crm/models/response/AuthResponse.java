package com.services.crm.models.response;

/**
 * DTO unificado de autenticación (login y registro).
 *
 * @param isVerified siempre true en login/registro exitoso
 * @param jwt        token JWT para autenticar siguientes peticiones (24h)
 * @param user       datos del usuario autenticado
 * @param tenant     datos de la tienda asociada
 * @param role       rol del usuario en esa sucursal
 */
public record AuthResponse(
    boolean isVerified,
    String jwt,
    UserDTO user,
    TenantDTO tenant,
    String role
) {}
