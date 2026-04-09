package com.services.crm.service;

import com.services.crm.models.response.CashSessionDTO;
import com.services.crm.models.response.DashboardResponse;
import com.services.crm.repository.CashSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio del dashboard.
 * Usa proyecciones JPQL — no carga entidades completas para lectura.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** Repositorio con proyecciones directas a CashSessionDTO */
    private final CashSessionRepository cashSessionRepository;

    /**
     * Obtiene los datos del dashboard para un tenant.
     * Cada query retorna directamente un DTO (no entidad → mapper).
     * Resultado: 2 queries máximo, 0 entidades cargadas.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData(UUID tenantId) {
        // Proyección directa a DTO — no carga entidad CashSession completa
        Optional<CashSessionDTO> openSession = cashSessionRepository
                .findOpenSessionDtoByTenantId(tenantId);

        Optional<CashSessionDTO> lastClosed = cashSessionRepository
                .findLastClosedSessionDtoByTenantId(tenantId);

        // Mensajes informativos para el frontend
        List<String> messages = new ArrayList<>();
        if (openSession.isEmpty()) {
            messages.add("No hay ninguna sesión de caja abierta. Por favor, abre una para comenzar a vender.");
        } else {
            messages.add("Sesión de caja activa desde " + openSession.get().openedAt());
        }

        return new DashboardResponse(
                openSession.isPresent(),
                openSession.orElse(null),
                lastClosed.orElse(null),
                messages
        );
    }
}
