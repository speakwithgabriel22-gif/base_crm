package com.services.crm.repository;

import com.services.crm.entity.SupplierTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio de transacciones con proveedores.
 */
@Repository
public interface SupplierTransactionRepository extends JpaRepository<SupplierTransaction, UUID> {

    /**
     * Lista transacciones de un proveedor en un tenant, ordenadas por fecha.
     */
    List<SupplierTransaction> findAllByTenantIdAndSupplierIdOrderByCreatedAtDesc(
            UUID tenantId, UUID supplierId);

    /**
     * Suma total de pagos en efectivo a proveedores en una sesión de caja.
     * COALESCE evita null — la fórmula de efectivo disponible lo necesita.
     */
    @Query("SELECT COALESCE(SUM(st.amount), 0) FROM SupplierTransaction st " +
            "WHERE st.cashSession.id = :cashSessionId " +
            "AND st.type = 'PAYMENT' AND st.regBorrado = 1")
    BigDecimal sumCashPaymentsByCashSessionId(@Param("cashSessionId") UUID cashSessionId);

}
