package com.services.crm.service;

import com.services.crm.entity.StoreProduct;
import com.services.crm.entity.Tenant;
import com.services.crm.entity.UpcCatalog;

import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.ProductRequest;
import com.services.crm.models.response.ProductDTO;
import com.services.crm.models.response.ProductLookupResponse;
import com.services.crm.repository.StoreProductRepository;
import com.services.crm.repository.SupplierRepository;
import com.services.crm.repository.UpcCatalogRepository;
import com.services.crm.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.services.crm.exception.BusinessException;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final StoreProductRepository storeProductRepository;
    private final UpcCatalogRepository upcCatalogRepository;
    private final SupplierRepository supplierRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(UUID tenantId, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return storeProductRepository.searchProducts(tenantId, search, pageable)
                    .map(entityMapper::toDto);
        }
        return storeProductRepository.findByTenantIdAndRegBorrado(tenantId, 1, pageable)
                .map(entityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDTO findById(UUID productId, UUID tenantId) {
        return storeProductRepository.findById(productId)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .map(entityMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    /**
     * Búsqueda rápida por UPC (escáner) o nombre.
     * Retorna escenario: LOCAL, GLOBAL o NOT_FOUND según el contrato.
     */
    @Transactional(readOnly = true)
    public ProductLookupResponse lookup(String query, UUID tenantId) {
        // 1. Buscar en inventario local del tenant
        return storeProductRepository.findByTenantIdAndUpcAndRegBorrado(tenantId, query, 1)
                .map(product -> new ProductLookupResponse(
                        "LOCAL",
                        product.getUpc(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        false // no requiere precio, ya existe
                ))
                .orElseGet(() -> {
                    // 2. Buscar en catálogo global
                    return upcCatalogRepository.findByUpc(query)
                            .map(catalog -> new ProductLookupResponse(
                                    "GLOBAL",
                                    catalog.getUpc(),
                                    catalog.getNombre(),
                                    null, // sin precio — el cajero debe ingresarlo
                                    null, // sin stock
                                    true // requiere precio
                    ))
                            .orElse(new ProductLookupResponse(
                                    "NOT_FOUND",
                                    query,
                                    null,
                                    null,
                                    null,
                                    true // requiere nombre, precio, stock
                    ));
                });
    }

    /**
     * Búsqueda en tiempo real (autocomplete) por nombre o UPC.
     * Retorna una lista combinada: primero inventario LOCAL, luego GLOBAL.
     */
    @Transactional(readOnly = true)
    public java.util.List<ProductLookupResponse> searchRealTime(String query, UUID tenantId) {
        Pageable limit = org.springframework.data.domain.PageRequest.of(0, 15);
        java.util.List<ProductLookupResponse> results = new java.util.ArrayList<>();
        java.util.Set<String> foundUpcs = new java.util.HashSet<>();

        // 1. Buscar en inventario local
        storeProductRepository.searchByTermRealTime(tenantId, query, limit).forEach(sp -> {
            results.add(new ProductLookupResponse(
                    "LOCAL",
                    sp.getUpc(),
                    sp.getName(),
                    sp.getPrice(),
                    sp.getStock(),
                    false));
            foundUpcs.add(sp.getUpc());
        });

        // 2. Buscar en catálogo global (excluyendo los que ya encontramos en local)
        upcCatalogRepository.searchGlobal(query, limit).forEach(cat -> {
            if (!foundUpcs.contains(cat.getUpc())) {
                results.add(new ProductLookupResponse(
                        "GLOBAL",
                        cat.getUpc(),
                        cat.getNombre(),
                        null,
                        null,
                        true));
            }
        });

        return results;
    }

    @Transactional
    public ProductDTO createOrUpdate(Tenant tenant, ProductRequest request) {
        if (request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_PRICE", "El precio debe ser mayor a cero",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        if (request.stock() != null && request.stock().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INVALID_STOCK", "El stock debe ser mayor o igual a cero",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // 1. Asegurar que existe en el catálogo global y actualizar el nombre si es
        // genérico
        UpcCatalog catalog = upcCatalogRepository.findByUpc(request.upc())
                .map(existingCatalog -> {
                    String currentName = existingCatalog.getNombre();
                    // Regla 2: Actualizar si el nombre es null, vacío o empieza con "Art. "
                    if (currentName == null || currentName.trim().isEmpty() || currentName.startsWith("Art. ")) {
                        existingCatalog.setNombre(request.name());
                        return upcCatalogRepository.save(existingCatalog);
                    }
                    return existingCatalog; // Retornar tal cual si el nombre ya es descriptivo
                })
                .orElseGet(() -> {
                    // Regla 1: Si no existe, crear uno nuevo
                    UpcCatalog newCatalog = UpcCatalog.builder()
                            .upc(request.upc())
                            .nombre(request.name())
                            .build();
                    return upcCatalogRepository.save(newCatalog);
                });

        // 2. Buscar si ya existe en la tienda
        StoreProduct product = storeProductRepository
                .findByTenantIdAndUpcAndRegBorrado(tenant.getId(), request.upc(), 1)
                .orElse(new StoreProduct());

        product.setTenant(tenant);
        product.setUpc(catalog.getUpc());
        product.setName(catalog.getNombre());
        if (catalog.getMeasurementUnit() != null) {
            product.setMeasurementUnit(catalog.getMeasurementUnit().name());
        }
        product.setPrice(request.price());
        product.setStock(request.stock());
        if (request.minStock() != null)
            product.setMinStock(request.minStock());
        if (request.costPrice() != null)
            product.setCostPrice(request.costPrice());

        if (request.supplierId() != null) {
            product.setSupplier(supplierRepository.findById(request.supplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado")));
        }

        product.setRegBorrado(1);
        product.setIsActive(true);
        product.setUpdatedAt(OffsetDateTime.now());

        return entityMapper.toDto(storeProductRepository.save(product));
    }

    @Transactional
    public void delete(UUID productId, UUID tenantId) {
        StoreProduct product = storeProductRepository.findById(productId)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        product.setRegBorrado(0);
        product.setUpdatedAt(OffsetDateTime.now());
        storeProductRepository.save(product);
    }

    @Transactional
    public void updateStock(UUID productId, java.math.BigDecimal quantity, UUID tenantId) {
        StoreProduct product = storeProductRepository.findById(productId)
                .filter(p -> p.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

        product.setStock(product.getStock().add(quantity));
        storeProductRepository.save(product);
    }
}
