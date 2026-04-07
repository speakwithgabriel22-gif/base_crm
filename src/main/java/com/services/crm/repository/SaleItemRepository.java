package com.services.crm.repository;

import com.services.crm.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for itemized sale details.
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    /**
     * Finds all items associated with a specific sale.
     * @param saleId The sale's ID.
     * @return List of line items.
     */
    List<SaleItem> findAllBySaleId(UUID saleId);
}
