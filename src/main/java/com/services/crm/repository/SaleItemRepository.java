package com.services.crm.repository;

import com.services.crm.entity.SaleItem;
import com.services.crm.models.response.SaleItemDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de items de venta.
 *
 * Estrategia:
 * - GET /sales/{id}/items → proyección JPQL directa a SaleItemDTO
 * - Escritura (POST /sales) → entidad completa (saveAll)
 */
@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    /**
     * Items de una venta — proyección directa a SaleItemDTO.
     * Solo trae: id, upc, productName, measurementUnit, quantity, unitPrice, subtotal.
     * NO carga: sale (entidad), upcCatalog (entidad completa), ivaAmount, iepsAmount.
     */
    @Query("SELECT new com.services.crm.models.response.SaleItemDTO(" +
           "CAST(si.id AS string), si.upcCatalog.upc, si.productName, " +
           "CAST(si.measurementUnit AS string), si.quantity, si.unitPrice, si.subtotal) " +
           "FROM SaleItem si WHERE si.sale.id = :saleId")
    List<SaleItemDTO> findAllDtoBySaleId(@Param("saleId") UUID saleId);

    /**
     * Items de una venta — entidad completa con upcCatalog precargado.
     * Solo para operaciones que necesitan la entidad (raro, pero disponible).
     */
    @EntityGraph(attributePaths = {"upcCatalog"})
    List<SaleItem> findAllBySaleId(UUID saleId);
}
