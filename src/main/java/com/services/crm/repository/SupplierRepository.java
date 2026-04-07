package com.services.crm.repository;

import com.services.crm.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for tenant-specific suppliers.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Finds all active suppliers for a specific tenant.
     * 
     * @param tenantId The store ID.
     * @return List of active suppliers.
     */
    List<Supplier> findAllByTenantIdAndIsActiveTrue(UUID tenantId);

}
