package com.services.crm.repository;

import com.services.crm.entity.ReceptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for detailing supplier receptions.
 */
@Repository
public interface ReceptionItemRepository extends JpaRepository<ReceptionItem, UUID> {

    /**
     * Lists all items received in a specific transaction.
     * @param transactionId The ID of the supplier transaction.
     * @return List of reception details.
     */
    List<ReceptionItem> findAllByTransactionId(UUID transactionId);
}
