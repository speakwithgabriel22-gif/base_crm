package com.services.crm.repository;

import com.services.crm.entity.UpcCatalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for global UPC catalog data.
 * This is a shared resource across all tenants.
 */
@Repository
public interface UpcCatalogRepository extends JpaRepository<UpcCatalog, String> {
    
    /**
     * Find a product by its unique barcode (UPC).
     * @param upc The barcode to search for.
     * @return Optional containing the product if found.
     */
    Optional<UpcCatalog> findByUpc(String upc);
    
    /**
     * Check if a UPC exists in the global catalog.
     * @param upc The barcode to check.
     * @return true if exists.
     */
    boolean existsByUpc(String upc);
}
