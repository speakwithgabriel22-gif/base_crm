package com.services.crm.repository;

import com.services.crm.entity.StoreProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

/**
 * Repository for local store products (inventory).
 * Data is isolated per tenant.
 */
@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, UUID> {

        /**
         * Finds a product in a specific tenant's inventory by its UPC.
         * Usa PESSIMISTIC_WRITE para evitar condiciones de carrera si dos ventas
         * concurrentes
         * intentan consultar y/o crear el mismo producto en StoreProduct al mismo
         * tiempo.
         */
        @Lock(LockModeType.PESSIMISTIC_WRITE)
        Optional<StoreProduct> findByTenantIdAndUpcCatalogUpcAndRegBorrado(UUID tenantId, String upc, Integer regBorrado);

        /**
         * Lists all inventory for a specific tenant.
         */
        Page<StoreProduct> findByTenantIdAndRegBorrado(UUID tenantId, Integer regBorrado, Pageable pageable);

        /**
         * Lists all products with low stock for alerts.
         */
        List<StoreProduct> findAllByTenantIdAndStockLessThanEqualAndRegBorrado(UUID tenantId,
                        java.math.BigDecimal minStock, Integer regBorrado);

        /**
         * Finds a product by UPC or Name in a specific tenant's inventory (SEARCH).
         */
        @Query("SELECT sp FROM StoreProduct sp " +
                        "WHERE sp.tenant.id = :tenantId AND (sp.upcCatalog.upc = :term OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :term, '%'))) "
                        +
                        "AND sp.regBorrado = 1")
        Page<StoreProduct> searchProducts(@Param("tenantId") UUID tenantId, @Param("term") String term,
                        Pageable pageable);

        /**
         * Búsqueda en tiempo real por UPC o Nombre en el inventario local.
         */
        @Query("SELECT sp FROM StoreProduct sp " +
                        "WHERE sp.tenant.id = :tenantId AND (sp.upcCatalog.upc LIKE CONCAT('%', :term, '%') OR LOWER(sp.name) LIKE LOWER(CONCAT('%', :term, '%'))) "
                        +
                        "AND sp.regBorrado = 1")
        List<StoreProduct> searchByTermRealTime(@Param("tenantId") UUID tenantId, @Param("term") String term,
                        Pageable pageable);

}
