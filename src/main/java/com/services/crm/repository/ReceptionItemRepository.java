package com.services.crm.repository;

import com.services.crm.entity.ReceptionItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de items de recepción de mercancía.
 */
@Repository
public interface ReceptionItemRepository extends JpaRepository<ReceptionItem, UUID> {

    /**
     * Lista items recibidos en una transacción de proveedor.
     * @EntityGraph precarga upcCatalog para mostrar nombre del producto sin N+1.
     */
    @EntityGraph(attributePaths = {"upcCatalog"})
    List<ReceptionItem> findAllByTransactionId(UUID transactionId);
}
