package com.services.crm.repository;

import com.services.crm.entity.RegistroFolio;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface RegistroFolioRepository extends JpaRepository<RegistroFolio, UUID> {

    // 🆕 Método adicional sin bloqueo (para solo consultar)
    Optional<RegistroFolio> findByTenantIdAndTipoAndFecha(UUID tenantId, String tipo, LocalDate fecha);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM RegistroFolio r WHERE r.tenant.id = :tenantId AND r.tipo = :tipo AND r.fecha = :fecha")
    Optional<RegistroFolio> findForUpdate(@Param("tenantId") UUID tenantId,
            @Param("tipo") String tipo,
            @Param("fecha") LocalDate fecha);
}