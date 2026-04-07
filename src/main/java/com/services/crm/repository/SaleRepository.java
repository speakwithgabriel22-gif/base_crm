package com.services.crm.repository;

import com.services.crm.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for transaction headers (Sales).
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /**
     * Lists all sales for a specific tenant in descending order.
     * @param tenantId The store ID.
     * @return List of sales.
     */
    List<Sale> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    /**
     * Finds a sale by its unique folio within a tenant.
     * @param tenantId The store ID.
     * @param folio The folio number.
     * @return Optional sale.
     */
    Optional<Sale> findByTenantIdAndFolio(UUID tenantId, String folio);
}
