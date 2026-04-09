package com.services.crm.controller;

import com.services.crm.entity.User;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.SaleRequest;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.SaleDTO;
import com.services.crm.models.response.SaleItemDTO;
import com.services.crm.repository.UserRepository;
import com.services.crm.repository.TenantRepository;
import com.services.crm.security.AuthContext;
import com.services.crm.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 🧾 Ventas (POS) — Punto de venta.
 *
 * Gestiona el flujo completo de ventas:
 * - Crear venta: valida stock, reduce inventario, actualiza caja, genera folio
 * - Historial: ventas de la sesión actual o por filtro
 * - Detalle: items de una venta para ticket digital o factura
 *
 * Reglas de negocio:
 * - No se puede vender sin stock suficiente
 * - Toda venta pertenece a una sesión de caja abierta
 * - Folio único formato SAL-YYYYMMDD-0001
 * - El total de la sesión se actualiza según el método de pago
 *
 * Base path: /api/v1/sales
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    /** Servicio de ventas con validación de stock y generación de folio */
    private final SaleService saleService;

    /** Repositorio de usuarios para obtener la entidad User desde el JWT */
    private final UserRepository userRepository;

    /** Helper para extraer userId/tenantId del JWT */
    private final AuthContext authContext;

    /** Repositorio de Tenants */
    private final TenantRepository tenantRepository;

    /**
     * POST /api/v1/sales
     *
     * Crear una nueva venta en el POS.
     *
     * Flujo completo:
     * 1. Valida que haya una sesión de caja abierta
     * 2. Valida stock disponible para cada item
     * 3. Reduce stock de cada producto (StoreProduct.stock -= quantity)
     * 4. Calcula el total de la venta (sum de unitPrice × quantity)
     * 5. Actualiza la sesión de caja según método de pago:
     * - CASH → cashTotal += total
     * - CARD → cardTotal += total
     * - TRANSFER → transferTotal += total
     * 6. Genera folio único SAL-YYYYMMDD-0001
     * 7. Persiste Sale + SaleItems
     *
     * @param request SaleRequest con cashSessionId, paymentType e items
     * @return ApiResponse<SaleDTO> con la venta creada y su folio
     * @throws com.services.crm.exception.BusinessException si no hay stock o no hay
     *                                                      caja abierta
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SaleDTO>> create(
            @Valid @RequestBody SaleRequest request) {

        User user = userRepository.findById(authContext.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        com.services.crm.entity.Tenant tenant = tenantRepository.getReferenceById(authContext.getTenantId());

        SaleDTO sale = saleService.createSale(user, tenant, request);
        return ResponseEntity.ok(ApiResponse.success(sale));
    }

    /**
     * GET /api/v1/sales?cashSessionId={id}
     *
     * Historial de ventas.
     * - Sin filtro: trae ventas de la sesión de caja activa (o todas del tenant)
     * - Con cashSessionId: filtra por sesión específica
     *
     * Nota: el listado NO precarga items (optimización).
     * Para ver los items de una venta, usar GET /sales/{id}/items.
     *
     * @param cashSessionId opcional — filtrar por sesión de caja
     * @return ApiResponse<List<SaleDTO>> con la lista de ventas (sin items para
     *         rendimiento)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SaleDTO>>> list(
            @RequestParam(required = false) UUID cashSessionId) {
        List<SaleDTO> sales = saleService.findAll(authContext.getTenantId(), cashSessionId);
        return ResponseEntity.ok(ApiResponse.success(sales));
    }

    /**
     * GET /api/v1/sales/{id}/items
     *
     * Detalle de productos vendidos en una venta específica.
     * Usado para generar ticket digital o factura.
     *
     * Optimización: usa @EntityGraph para precargar upcCatalog
     * y evitar N+1 (una query SQL en vez de N+1 por cada item).
     *
     * @param id UUID de la venta
     * @return ApiResponse<List<SaleItemDTO>> con los items de la venta
     * @throws com.services.crm.exception.ResourceNotFoundException si la venta no
     *                                                              existe
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<ApiResponse<List<SaleItemDTO>>> getItems(
            @PathVariable UUID id) {
        List<SaleItemDTO> items = saleService.getItems(id);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
