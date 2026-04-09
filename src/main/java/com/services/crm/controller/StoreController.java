package com.services.crm.controller;

import com.services.crm.entity.Tenant;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.TenantDTO;
import com.services.crm.repository.TenantRepository;
import com.services.crm.security.AuthContext;
import com.services.crm.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 🏪 Mi Tienda — Configuración del negocio.
 *
 * Permite al dueño (OWNER) actualizar los datos de su tienda:
 * nombre comercial, dirección, teléfono y email de contacto.
 *
 * Base path: /api/v1/store
 * Autenticación: JWT requerido (OWNER recomendado)
 */
@RestController
@RequestMapping("/api/v1/store")
@RequiredArgsConstructor
public class StoreController {

    /** Repositorio de tenant para leer y persistir cambios */
    private final TenantRepository tenantRepository;

    /** Helper para extraer tenantId del JWT */
    private final AuthContext authContext;

    /** Mapper para convertir la entidad actualizada a DTO */
    private final EntityMapper entityMapper;

    /**
     * PUT /api/v1/store
     *
     * Actualizar datos del negocio.
     *
     * Campos editables:
     * - name: nombre comercial de la tienda
     * - email: correo de contacto
     * - phone: teléfono de contacto
     *
     * El tenantId se toma del JWT — no se puede cambiar de tienda.
     *
     * @param updates Tenant parcial con los campos a actualizar
     * @return ApiResponse<TenantDTO> con los datos actualizados
     * @throws com.services.crm.exception.ResourceNotFoundException si el tenant no
     *                                                              existe
     */
    @PutMapping
    public ResponseEntity<ApiResponse<TenantDTO>> update(
            @RequestBody Tenant updates) {

        Tenant tenant = tenantRepository.findById(authContext.getTenantId())
                .orElseThrow(() -> new ResourceNotFoundException("Tienda no encontrada"));

        // Solo actualizar los campos editables, nunca el ID ni datos de suscripción
        if (updates.getName() != null)
            tenant.setName(updates.getName());
        if (updates.getEmail() != null)
            tenant.setEmail(updates.getEmail());
        if (updates.getPhone() != null)
            tenant.setPhone(updates.getPhone());
        if (updates.getAddress() != null)
            tenant.setAddress(updates.getAddress());

        tenant = tenantRepository.save(tenant);
        return ResponseEntity.ok(ApiResponse.success(entityMapper.toDto(tenant)));
    }
}
