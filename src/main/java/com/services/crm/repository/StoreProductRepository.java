package com.services.crm.repository;

import com.services.crm.entity.StoreProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for local store products (inventory).
 * Data is isolated per tenant.
 */
@Repository
public interface StoreProductRepository extends JpaRepository<StoreProduct, UUID> {

    /**
     * Finds a product in a specific tenant's inventory by its UPC.
     * @param tenantId The store ID.
     * @param upc The barcode.
     * @return Optional containing the local store product.
     */
    Optional<StoreProduct> findByTenantIdAndUpcCatalogUpc(UUID tenantId, String upc);

    /**
     * Lists all inventory for a specific tenant.
     * @param tenantId The store ID.
     * @return List of store products.
     */
    List<StoreProduct> findAllByTenantId(UUID tenantId);

    /**
     * Lists all products with low stock for alerts.
     * @param tenantId The store ID.
     * @return List of products needing replenishment.
     */
    List<StoreProduct> findAllByTenantIdAndStockLessThanEqual(UUID tenantId, Integer minStock);
}
