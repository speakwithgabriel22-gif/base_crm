package com.services.crm.repository;

import com.services.crm.entity.SupplierTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for transactions between the tenant and its suppliers.
 */
@Repository
public interface SupplierTransactionRepository extends JpaRepository<SupplierTransaction, UUID> {

    /**
     * Lists all transactions for a specific tenant and supplier.
     * @param tenantId The store ID.
     * @param supplierId The supplier's ID.
     * @return Ordered list of transactions.
     */
    List<SupplierTransaction> findAllByTenantIdAndSupplierIdOrderByCreatedAtDesc(UUID tenantId, UUID supplierId);
}
