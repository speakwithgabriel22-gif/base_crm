package com.services.crm.controller;

import com.services.crm.entity.Tenant;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.ExpenseRequest;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.ExpenseDTO;
import com.services.crm.repository.TenantRepository;
import com.services.crm.security.AuthContext;
import com.services.crm.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 💸 Gastos Operativos — Registro de gastos de la tienda.
 *
 * Permite registrar gastos operativos (luz, agua, sueldos, etc.)
 * asociados a la sesión de caja activa.
 *
 * Reglas de negocio:
 * - Si el gasto es en efectivo (CASH), se valida que haya
 * saldo suficiente en la caja antes de registrarlo
 * - Fórmula: disponible = initialAmount + cashSales - gastos_cash -
 * pagos_proveedor_cash
 * - Todo gasto pertenece a una sesión de caja
 *
 * Base path: /api/v1/expenses
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    /** Servicio de gastos con validación de efectivo disponible */
    private final ExpenseService expenseService;

    /** Repositorio de tenant para obtener la entidad en operaciones de escritura */
    private final TenantRepository tenantRepository;

    /** Helper para extraer tenantId del JWT */
    private final AuthContext authContext;

    /**
     * POST /api/v1/expenses
     *
     * Registrar un gasto operativo.
     *
     * Validaciones:
     * - cashSessionId: debe corresponder a una sesión abierta
     * - category: obligatorio (ej: "Luz", "Agua", "Sueldos")
     * - amount > 0
     * - paymentMethod: opcional, default "CASH"
     * - Si CASH → valida saldo suficiente en caja
     * - Si CARD o TRANSFER → no afecta el efectivo del cajón
     *
     * @param request ExpenseRequest con datos del gasto
     * @return ApiResponse<ExpenseDTO> con el gasto registrado
     * @throws com.services.crm.exception.InsufficientCashException si no hay
     *                                                              efectivo
     *                                                              suficiente
     * @throws com.services.crm.exception.BusinessException         si la sesión de
     *                                                              caja no existe o
     *                                                              está cerrada
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseDTO>> create(
            @Valid @RequestBody ExpenseRequest request) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        ExpenseDTO expense = expenseService.create(tenant, request);
        return ResponseEntity.ok(ApiResponse.success(expense));
    }
}
