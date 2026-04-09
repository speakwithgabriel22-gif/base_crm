package com.services.crm.service;

import com.services.crm.entity.Supplier;
import com.services.crm.entity.Tenant;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.response.SupplierDTO;
import com.services.crm.repository.SupplierRepository;
import com.services.crm.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import java.time.OffsetDateTime;

/**
 * Servicio de proveedores.
 * Lecturas usan proyección JPQL directo a DTO (no carga entidad completa).
 * Escrituras usan entidad para persistencia.
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

    /** Repositorio de proveedores con queries optimizadas */
    private final SupplierRepository supplierRepository;

    /** Mapper para convertir entidad → DTO en operaciones de escritura */
    private final EntityMapper entityMapper;

    /**
     * Lista proveedores activos del tenant.
     * Usa proyección JPQL — solo trae las columnas de SupplierDTO, no la entidad
     * completa.
     */
    @Transactional(readOnly = true)
    public List<SupplierDTO> findAll(UUID tenantId) {
        return supplierRepository.findAllDtoByTenantId(tenantId);
    }

    /**
     * Busca un proveedor por ID validando que pertenezca al tenant.
     * Usa query optimizada con filtro directo por tenantId (no carga y luego
     * filtra).
     */
    @Transactional(readOnly = true)
    public SupplierDTO findById(UUID supplierId, UUID tenantId) {
        return supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .map(entityMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
    }

    /**
     * Crea o actualiza un proveedor del tenant.
     * Retorna DTO via mapper porque necesita persistir la entidad primero.
     */
    @Transactional
    public SupplierDTO createOrUpdate(Tenant tenant, Supplier supplier) {
        supplier.setTenant(tenant);
        supplier.setRegBorrado(1);
        return entityMapper.toDto(supplierRepository.save(supplier));
    }

    /**
     * Soft delete de proveedor (reg_borrado = 0).
     * Valida pertenencia al tenant antes de eliminar.
     */
    @Transactional
    public void delete(UUID supplierId, UUID tenantId) {
        Supplier supplier = supplierRepository.findByIdAndTenantId(supplierId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        supplier.setRegBorrado(0);
        supplier.setUpdatedAt(OffsetDateTime.now());
        supplierRepository.save(supplier);
    }
}
