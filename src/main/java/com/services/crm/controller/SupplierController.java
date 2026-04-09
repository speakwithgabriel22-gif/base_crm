package com.services.crm.controller;

import com.services.crm.entity.Supplier;
import com.services.crm.entity.Tenant;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.SupplierDTO;
import com.services.crm.repository.TenantRepository;
import com.services.crm.security.AuthContext;
import com.services.crm.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 🏢 Proveedores y Cuentas por Pagar.
 *
 * Gestiona el catálogo de proveedores del tenant,
 * recepción de mercancía (que aumenta stock y deuda)
 * y pagos/abonos (que reducen deuda y pueden afectar caja si son en efectivo).
 *
 * Reglas de negocio:
 * - Un pago en efectivo valida que haya saldo suficiente en caja
 * - La recepción de mercancía actualiza stock y costo del producto
 * - Soft delete (reg_borrado) en eliminación
 *
 * Base path: /api/v1/suppliers
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    /** Servicio de proveedores con proyecciones JPQL optimizadas */
    private final SupplierService supplierService;

    /** Repositorio de tenant para obtener la entidad en operaciones de escritura */
    private final TenantRepository tenantRepository;

    /** Helper para extraer tenantId del JWT */
    private final AuthContext authContext;

    /**
     * GET /api/v1/suppliers
     *
     * Listar todos los proveedores activos del tenant.
     * Incluye la deuda actual (totalDebt) de cada proveedor.
     *
     * Optimización: usa proyección JPQL directa a SupplierDTO.
     * No carga entidades completas — solo las columnas que el DTO necesita.
     *
     * @return ApiResponse<List<SupplierDTO>> con proveedores y sus deudas
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplierDTO>>> list() {
        List<SupplierDTO> suppliers = supplierService.findAll(authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(suppliers));
    }

    /**
     * POST /api/v1/suppliers
     *
     * Crear un nuevo proveedor para la tienda.
     * Se vincula automáticamente al tenant del usuario autenticado.
     *
     * Campos opcionales: phone, category, visitDay, deliveryDay, contactName
     *
     * @param supplier datos del proveedor (el tenantId se toma del JWT, no del body)
     * @return ApiResponse<SupplierDTO> con el proveedor creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SupplierDTO>> create(
            @Valid @RequestBody Supplier supplier) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        SupplierDTO result = supplierService.createOrUpdate(tenant, supplier);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * PUT /api/v1/suppliers/{id}
     *
     * Editar datos de un proveedor existente.
     * Solo se pueden editar: nombre, teléfono, contactName, category,
     * visitDay, deliveryDay, frequency.
     *
     * @param id UUID del proveedor a editar
     * @param supplier datos actualizados del proveedor
     * @return ApiResponse<SupplierDTO> con el proveedor actualizado
     * @throws com.services.crm.exception.ResourceNotFoundException si no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SupplierDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody Supplier supplier) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        supplier.setId(id);
        SupplierDTO result = supplierService.createOrUpdate(tenant, supplier);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * DELETE /api/v1/suppliers/{id}
     *
     * Eliminación lógica del proveedor (reg_borrado = 0).
     * El proveedor deja de aparecer en listados pero no se borra de la BD.
     *
     * @param id UUID del proveedor a eliminar
     * @return ApiResponse<Void> confirmación
     * @throws com.services.crm.exception.ResourceNotFoundException si no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        supplierService.delete(id, authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * POST /api/v1/suppliers/{id}/receive
     *
     * Recepción de mercancía (compra al proveedor).
     *
     * Efectos:
     * - Aumenta stock del producto (StoreProduct.stock += quantity)
     * - Actualiza costo del producto si se proporciona
     * - Aumenta deuda con el proveedor (Supplier.totalDebt += amount)
     * - Registra SupplierTransaction de tipo RECEPTION
     *
     * @param id UUID del proveedor
     * @return ApiResponse<Void> confirmación (TODO: implementar en servicio)
     */
    @PostMapping("/{id}/receive")
    public ResponseEntity<ApiResponse<Void>> receive(@PathVariable UUID id) {
        // TODO: Implementar SupplierService.receiveGoods(tenantId, supplierId, items)
        // Por ahora retorna success vacío para documentar el endpoint
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * POST /api/v1/suppliers/{id}/pay
     *
     * Realizar un pago (abono) al proveedor.
     *
     * Efectos:
     * - Reduce deuda del proveedor (Supplier.totalDebt -= amount)
     * - Si es en efectivo: valida saldo suficiente en caja
     * - Registra SupplierTransaction de tipo PAYMENT
     *
     * Regla: solo pagar en efectivo si hay efectivo suficiente en caja.
     * Fórmula: disponible = initialAmount + cashSales - gastos_cash - pagos_proveedor_cash
     *
     * @param id UUID del proveedor
     * @return ApiResponse<Void> confirmación (TODO: implementar en servicio)
     */
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<Void>> pay(@PathVariable UUID id) {
        // TODO: Implementar SupplierService.pay(tenantId, supplierId, amount, paymentMethod)
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
