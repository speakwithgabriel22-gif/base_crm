package com.services.crm.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper para extraer datos del usuario autenticado desde el JWT.
 * Evita repetir la lógica de parsing de claims en cada controller.
 * El userId viene como subject, el tenantId como claim.
 */
@Component
@RequiredArgsConstructor
public class AuthContext {

    private final JwtUtil jwtUtil;

    /**
     * Extrae el userId del token JWT del SecurityContext.
     * @return UUID del usuario autenticado
     * @throws IllegalStateException si no hay usuario autenticado
     */
    public UUID getUserId() {
        String principal = (String) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return UUID.fromString(principal);
    }

    /**
     * Extrae el tenantId del token JWT.
     * Se obtiene del header Authorization (no del SecurityContext porque
     * Spring Security no lo guarda en el principal).
     * @return UUID del tenant del usuario autenticado
     */
    public UUID getTenantId() {
        String token = getTokenFromContext();
        Claims claims = jwtUtil.extractAllClaims(token);
        return UUID.fromString(claims.get("tenantId", String.class));
    }

    /**
     * Extrae el rol del usuario desde el JWT.
     * @return role como String (OWNER/AGENT)
     */
    public String getRole() {
        String token = getTokenFromContext();
        Claims claims = jwtUtil.extractAllClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * Obtiene el token JWT crudo del SecurityContext.
     * El JwtFilter guarda el userId como principal, pero necesitamos
     * el token completo para extraer claims adicionales.
     */
    private String getTokenFromContext() {
        // El token se puede recuperar del request actual via RequestContextHolder
        jakarta.servlet.http.HttpServletRequest request =
            ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()).getRequest();
        String authHeader = request.getHeader("Authorization");
        return authHeader.substring(7); // quitar "Bearer "
    }
}
