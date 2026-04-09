package com.services.crm.repository;

import com.services.crm.entity.Sale;
import com.services.crm.models.response.SaleDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de ventas (cabecera).
 *
 * Estrategia:
 * - Listados (GET /sales) → proyección JPQL directa a SaleDTO (sin items, sin entidad)
 * - Detalle (GET /sales/{id}) → @EntityGraph con items precargados
 * - Escritura (POST /sales) → entidad completa
 */
@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {

    /**
     * Lista ventas del tenant — proyección directa a SaleDTO.
     * Solo trae: id, cashSessionId, folio, paymentType, total, createdAt.
     * NO carga: tenant, user, cashSession (entidades), items, timestamps de update.
     * Items se obtienen por separado con GET /sales/{id}/items.
     */
    @Query("SELECT new com.services.crm.models.response.SaleDTO(" +
           "CAST(s.id AS string), CAST(s.cashSession.id AS string), " +
           "s.folio, CAST(s.paymentType AS string), s.total, s.createdAt, " +
           "null) " +
           "FROM Sale s WHERE s.tenant.id = :tenantId AND s.regBorrado = 1 " +
           "ORDER BY s.createdAt DESC")
    List<SaleDTO> findAllDtoByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Lista ventas de una sesión de caja — proyección directa a SaleDTO.
     * Misma optimización que findAllDtoByTenantId pero filtrado por cashSessionId.
     */
    @Query("SELECT new com.services.crm.models.response.SaleDTO(" +
           "CAST(s.id AS string), CAST(s.cashSession.id AS string), " +
           "s.folio, CAST(s.paymentType AS string), s.total, s.createdAt, " +
           "null) " +
           "FROM Sale s WHERE s.cashSession.id = :cashSessionId AND s.regBorrado = 1 " +
           "ORDER BY s.createdAt DESC")
    List<SaleDTO> findAllDtoByCashSessionId(@Param("cashSessionId") UUID cashSessionId);

    /**
     * Busca venta por folio con items precargados — para detalle/ticket.
     * @EntityGraph evita N+1 (1 query en vez de N+1 por cada item).
     */
    @EntityGraph(attributePaths = {"items", "items.upcCatalog"})
    Optional<Sale> findByTenantIdAndFolio(UUID tenantId, String folio);

    /**
     * Busca venta por ID con items precargados — para detalle de venta.
     */
    @EntityGraph(attributePaths = {"items", "items.upcCatalog"})
    @Query("SELECT s FROM Sale s WHERE s.id = :id")
    Optional<Sale> findByIdWithItems(@Param("id") UUID id);
}
