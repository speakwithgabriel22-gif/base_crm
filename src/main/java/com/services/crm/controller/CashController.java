package com.services.crm.controller;

import com.services.crm.entity.User;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.CashCloseRequest;
import com.services.crm.models.request.CashOpenRequest;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.CashCloseResponse;
import com.services.crm.models.response.CashSessionDTO;
import com.services.crm.repository.UserRepository;
import com.services.crm.repository.TenantRepository;
import com.services.crm.security.AuthContext;
import com.services.crm.service.CashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 💰 Gestión de Caja — Sesión única por tienda.
 *
 * Controla la apertura y cierre de sesiones de caja.
 * Regla de negocio: solo UNA sesión de caja abierta por tenant a la vez.
 * Si se intenta abrir otra, el servicio lanza
 * BusinessException("ALREADY_OPEN").
 *
 * Base path: /api/v1/cash
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController {

    /** Servicio de caja con validación de sesión única y cálculo de efectivo */
    private final CashService cashService;

    /** Repositorio de usuarios para obtener la entidad User desde el JWT */
    private final UserRepository userRepository;

    /** Helper para extraer userId/tenantId del JWT */
    private final AuthContext authContext;

    /** Repositorio de tenants */
    private final TenantRepository tenantRepository;

    /**
     * POST /api/v1/cash/open
     *
     * Abrir una nueva sesión de caja.
     *
     * Validaciones:
     * - initialAmount >= 0 (validación por @PositiveOrZero en el DTO)
     * - No debe haber otra sesión abierta para este tenant
     *
     * @param request CashOpenRequest con el monto inicial del fondo de caja
     * @return ApiResponse<CashSessionDTO> con los datos de la sesión creada
     * @throws com.services.crm.exception.BusinessException si ya hay una caja
     *                                                      abierta
     */
    @PostMapping("/open")
    public ResponseEntity<ApiResponse<CashSessionDTO>> open(
            @Valid @RequestBody CashOpenRequest request) {

        // Obtener entidad User
        User user = userRepository.findById(authContext.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        com.services.crm.entity.Tenant tenant = tenantRepository.getReferenceById(authContext.getTenantId());

        CashSessionDTO session = cashService.openSession(user, tenant, request);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    /**
     * POST /api/v1/cash/close
     *
     * Cerrar la sesión de caja activa.
     *
     * Recibe el efectivo contado físicamente (actualCash).
     * El servicio calcula la desviación entre el efectivo esperado y el contado.
     *
     * @param request CashCloseRequest con el monto real contado en caja
     * @return ApiResponse<CashSessionDTO> con los datos de la sesión cerrada
     * @throws com.services.crm.exception.BusinessException si no hay sesión abierta
     *                                                      o ya está cerrada
     */
    @PostMapping("/close")
    public ResponseEntity<ApiResponse<CashCloseResponse>> close(
            @Valid @RequestBody CashCloseRequest request) {

        // Obtener la sesión abierta del tenant para cerrarla
        CashCloseResponse result = cashService.closeSession(
                cashService.getOpenSessionEntity(authContext.getTenantId()).getId(),
                request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
