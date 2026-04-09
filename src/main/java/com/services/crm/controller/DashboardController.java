package com.services.crm.controller;

import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.DashboardResponse;
import com.services.crm.security.AuthContext;
import com.services.crm.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 📊 Dashboard — Pantalla de inicio del POS.
 *
 * Proporciona un snapshot del estado actual de la tienda:
 * sesión de caja activa, resumen de la última sesión cerrada,
 * y mensajes relevantes (stock crítico, visitas de proveedores, etc.).
 *
 * Base path: /api/v1/dashboard
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** Servicio que agrega datos de caja usando proyecciones JPQL directas */
    private final DashboardService dashboardService;

    /** Helper para extraer tenantId del JWT sin repetir lógica */
    private final AuthContext authContext;

    /**
     * GET /api/v1/dashboard/init
     *
     * Obtener estado actual de la tienda para la pantalla de inicio.
     *
     * Respuesta incluye:
     * - hasOpenSession: boolean indicando si hay caja abierta
     * - openSession: datos de la sesión activa (null si no hay)
     * - lastClosedSession: resumen de la última caja cerrada
     * - messages: lista de mensajes cortos (stock crítico, fin de trial, etc.)
     *
     * Optimización: usa proyecciones JPQL directas a DTO (no carga entidades).
     *
     * @return ApiResponse<DashboardResponse> con el snapshot del estado actual
     */
    @GetMapping("/init")
    public ResponseEntity<ApiResponse<DashboardResponse>> init() {
        // tenantId viene del JWT — cada usuario solo ve datos de su tienda
        DashboardResponse data = dashboardService.getDashboardData(authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
