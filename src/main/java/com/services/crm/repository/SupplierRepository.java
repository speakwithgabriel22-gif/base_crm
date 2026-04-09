package com.services.crm.repository;

import com.services.crm.entity.Supplier;
import com.services.crm.models.response.SupplierDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio de proveedores.
 * Usa proyecciones JPQL para solo traer las columnas que el DTO necesita.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Lista proveedores activos del tenant — proyección directa a SupplierDTO.
     * Solo trae: id, name, contactName, phone, category, visitDay, deliveryDay, totalDebt.
     * No carga: tenant, timestamps, isActive, regBorrado (no los necesitamos en la respuesta).
     */
    @Query("SELECT new com.services.crm.models.response.SupplierDTO(" +
           "CAST(s.id AS string), s.name, s.contactName, s.phone, " +
           "s.category, s.visitDay, s.deliveryDay, s.frequency, s.totalDebt) " +
           "FROM Supplier s WHERE s.tenant.id = :tenantId AND s.regBorrado = 1")
    List<SupplierDTO> findAllDtoByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Busca proveedor por ID — entidad completa. Se usa para operaciones de escritura.
     */
    @Query("SELECT s FROM Supplier s WHERE s.id = :id AND s.tenant.id = :tenantId AND s.regBorrado = 1")
    java.util.Optional<Supplier> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    /**
     * Lista proveedores activos (entidad) — solo para operaciones de escritura.
     */
    List<Supplier> findAllByTenantIdAndRegBorrado(UUID tenantId, Integer regBorrado);
}
