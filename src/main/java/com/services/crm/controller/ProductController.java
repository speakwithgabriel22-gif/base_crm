package com.services.crm.controller;

import com.services.crm.models.request.ProductRequest;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.ProductDTO;
import com.services.crm.models.response.ProductLookupResponse;
import com.services.crm.security.AuthContext;
import com.services.crm.service.ProductService;
import com.services.crm.entity.Tenant;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.repository.TenantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * 🏷️ Inventario y Productos.
 *
 * Gestiona el catálogo de productos del tenant:
 * búsqueda rápida por escáner, CRUD completo, y ajuste de stock.
 *
 * La búsqueda por escáner (lookup) retorna uno de 3 escenarios:
 * - LOCAL: el producto ya existe en el inventario de la tienda
 * - GLOBAL: existe en el catálogo global pero no en esta tienda
 * - NOT_FOUND: el UPC no existe en ningún lado
 *
 * Base path: /api/v1/products
 * Autenticación: JWT requerido (OWNER o AGENT)
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    /** Servicio de productos con validación de stock y catálogo */
    private final ProductService productService;

    /** Repositorio de tenant para obtener la entidad en operaciones de escritura */
    private final TenantRepository tenantRepository;

    /** Helper para extraer tenantId del JWT */
    private final AuthContext authContext;

    /**
     * GET /api/v1/products/lookup?q={barcode_o_nombre}
     *
     * Búsqueda rápida por escáner o nombre. Usado en el flujo de venta POS.
     *
     * Escenarios de respuesta:
     * - LOCAL: producto en inventario → devuelve precio y stock actual
     * - GLOBAL: en catálogo global pero no en esta tienda → el cajero puede
     * agregarlo
     * - NOT_FOUND: no existe → el cajero puede crearlo manualmente
     *
     * @param q código de barras (UPC) o fragmento de nombre a buscar
     * @return ApiResponse<ProductLookupResponse> con escenario, datos y si requiere
     *         precio
     */
    @GetMapping("/lookup")
    public ResponseEntity<ApiResponse<ProductLookupResponse>> lookup(
            @RequestParam String q) {
        // q puede ser UPC (escáner) o fragmento de nombre
        ProductLookupResponse result = productService.lookup(q, authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/v1/products/search?q={query}
     *
     * Búsqueda en tiempo real para autocompletado por nombre o UPC.
     * Retorna una lista de productos: primero de la tienda, luego globales.
     *
     * @param q término de búsqueda a coincidir
     * @return ApiResponse con List<ProductLookupResponse>
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<java.util.List<ProductLookupResponse>>> searchRealTime(
            @RequestParam String q) {
        java.util.List<ProductLookupResponse> results = productService.searchRealTime(q, authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * GET /api/v1/products?page=0&size=20&search={nombre_o_upc}
     *
     * Listado paginado de productos del inventario local.
     * Incluye estado de stock por producto: CRÍTICO, BAJO, OK
     * (calculado como stock vs minStock).
     *
     * Optimización: usa @EntityGraph para precargar upcCatalog (evita N+1).
     *
     * @param search   filtro opcional por nombre o UPC
     * @param pageable paginación (page, size, sort)
     * @return ApiResponse<Page<ProductDTO>> con lista paginada
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> list(
            @RequestParam(required = false) String search,
            Pageable pageable) {
        Page<ProductDTO> products = productService.findAll(
                authContext.getTenantId(), search, pageable);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * POST /api/v1/products
     *
     * Crear un nuevo producto en el inventario local.
     * Si el UPC no existe en el catálogo global (upc_catalog),
     * el servicio lo crea automáticamente.
     *
     * Validaciones:
     * - upc: obligatorio, código de barras
     * - name: obligatorio, nombre del producto
     * - price > 0
     * - stock >= 0
     * - costPrice: opcional (precio de compra al proveedor)
     *
     * @param request ProductRequest con datos del nuevo producto
     * @return ApiResponse<ProductDTO> con el producto creado
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(
            @Valid @RequestBody ProductRequest request) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        ProductDTO product = productService.createOrUpdate(tenant, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * PUT /api/v1/products/{id}
     *
     * Editar producto existente: precio, stock mínimo, activo/inactivo.
     * No permite cambiar el UPC (identificador único del producto).
     *
     * @param id      UUID del StoreProduct a editar
     * @param request ProductRequest con los campos a actualizar
     * @return ApiResponse<ProductDTO> con el producto actualizado
     * @throws com.services.crm.exception.ResourceNotFoundException si el producto
     *                                                              no existe
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> update(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tenant no encontrado"));

        // El servicio usa createOrUpdate — si el UPC existe, actualiza
        ProductDTO product = productService.createOrUpdate(tenant, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    /**
     * PATCH /api/v1/products/{id}/stock
     *
     * Ajustar stock por compra, ajuste manual o devolución.
     * NO afecta la sesión de caja (solo inventario).
     *
     * quantity positiva = entrada de stock (compra, devolución)
     * quantity negativa = salida de stock (merma, ajuste)
     *
     * @param id       UUID del StoreProduct
     * @param quantity cantidad a sumar (positiva) o restar (negativa)
     * @param reason   motivo del ajuste (COMPRA, DEVOLUCION, MERMA, AJUSTE)
     * @return ApiResponse<ProductDTO> con el stock actualizado
     */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @PathVariable UUID id,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false, defaultValue = "AJUSTE") String reason) {
        // updateStock suma o resta según el signo de quantity
        productService.updateStock(id, quantity, authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * DELETE /api/v1/products/{id}
     *
     * Eliminación lógica (soft delete) del producto.
     * Marca reg_borrado = 0 en lugar de eliminar el registro.
     * El producto deja de aparecer en listados y búsquedas.
     *
     * @param id UUID del StoreProduct a eliminar
     * @return ApiResponse<Void> confirmación de eliminación
     * @throws com.services.crm.exception.ResourceNotFoundException si el producto
     *                                                              no existe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id, authContext.getTenantId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
