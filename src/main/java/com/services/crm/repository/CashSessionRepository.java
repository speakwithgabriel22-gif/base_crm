package com.services.crm.repository;

import com.services.crm.entity.CashSession;
import com.services.crm.models.response.CashSessionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de sesiones de caja.
 * Proyecciones JPQL para dashboard (solo columnas del DTO).
 * Entidad completa solo para operaciones transaccionales (abrir/cerrar caja, registrar ventas).
 */
@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, UUID> {

    /**
     * Encuentra la caja abierta del tenant — ENTIDAD completa.
     * Se necesita la entidad para operaciones de escritura (cerrar, actualizar totales).
     */
    @Query("SELECT cs FROM CashSession cs " +
           "WHERE cs.tenant.id = :tenantId AND cs.closedAt IS NULL")
    Optional<CashSession> findByTenantIdAndClosedAtIsNull(@Param("tenantId") UUID tenantId);

    /**
     * Proyección de la caja abierta para DASHBOARD — solo columnas del DTO.
     * No carga tenant, user, ni campos que el DTO no usa.
     */
    @Query("SELECT new com.services.crm.models.response.CashSessionDTO(" +
           "CAST(cs.id AS string), cs.initialAmount, cs.totalSales, " +
           "cs.cashTotal, cs.cardTotal, cs.transferTotal, cs.openedAt, cs.closedAt) " +
           "FROM CashSession cs WHERE cs.tenant.id = :tenantId AND cs.closedAt IS NULL")
    Optional<CashSessionDTO> findOpenSessionDtoByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Última sesión cerrada para DASHBOARD — proyección directa a DTO.
     * Solo la más reciente (ORDER BY + LIMIT 1).
     */
    @Query("SELECT new com.services.crm.models.response.CashSessionDTO(" +
           "CAST(cs.id AS string), cs.initialAmount, cs.totalSales, " +
           "cs.cashTotal, cs.cardTotal, cs.transferTotal, cs.openedAt, cs.closedAt) " +
           "FROM CashSession cs WHERE cs.tenant.id = :tenantId AND cs.closedAt IS NOT NULL " +
           "ORDER BY cs.closedAt DESC LIMIT 1")
    Optional<CashSessionDTO> findLastClosedSessionDtoByTenantId(@Param("tenantId") UUID tenantId);
}
