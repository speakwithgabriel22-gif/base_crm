package com.services.crm.service;

import com.services.crm.entity.CashSession;
import com.services.crm.entity.User;
import com.services.crm.exception.BusinessException;
import com.services.crm.models.request.CashCloseRequest;
import com.services.crm.models.request.CashOpenRequest;
import com.services.crm.models.response.CashSessionDTO;
import com.services.crm.repository.CashSessionRepository;
import com.services.crm.repository.ExpenseRepository;
import com.services.crm.repository.SupplierTransactionRepository;
import com.services.crm.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashSessionRepository cashSessionRepository;
    private final ExpenseRepository expenseRepository;
    private final SupplierTransactionRepository supplierTransactionRepository;
    private final EntityMapper entityMapper;

    @Transactional(readOnly = true)
    public BigDecimal getAvailableCash(UUID cashSessionId) {
        CashSession session = cashSessionRepository.findById(cashSessionId)
                .orElseThrow(() -> new BusinessException("SESSION_NOT_FOUND", "Sesión de caja no encontrada",
                        org.springframework.http.HttpStatus.NOT_FOUND));

        BigDecimal initialAmount = session.getInitialAmount();
        BigDecimal cashSales = session.getCashTotal();

        // Manejo de null en caso de que los repositorios no usen COALESCE o no haya
        // registros
        BigDecimal expenses = java.util.Optional
                .ofNullable(expenseRepository.sumCashExpensesByCashSessionId(cashSessionId)).orElse(BigDecimal.ZERO);
        BigDecimal supplierPayments = java.util.Optional
                .ofNullable(supplierTransactionRepository.sumCashPaymentsByCashSessionId(cashSessionId))
                .orElse(BigDecimal.ZERO);

        // Fórmula: efectivo disponible = fondo + ventas_cash - gastos_cash -
        // pagos_proveedor_cash
        return initialAmount.add(cashSales).subtract(expenses).subtract(supplierPayments);
    }

    @Transactional
    public CashSessionDTO openSession(User user, com.services.crm.entity.Tenant tenant, CashOpenRequest request) {
        // 1. Validar que no haya una sesión abierta para el tenant
        cashSessionRepository.findByTenantIdAndClosedAtIsNull(tenant.getId())
                .ifPresent(s -> {
                    throw new BusinessException("ALREADY_OPEN", "Ya existe una sesión de caja abierta",
                            org.springframework.http.HttpStatus.BAD_REQUEST);
                });

        // 2. Crear sesión
        CashSession session = CashSession.builder()
                .tenant(tenant)
                .user(user)
                .initialAmount(request.initialAmount())
                .openedAt(OffsetDateTime.now())
                .build();

        return entityMapper.toDto(cashSessionRepository.save(session));
    }

    /**
     * Cierra la sesión de caja activa.
     * Calcula expectedCash usando la fórmula de efectivo disponible,
     * y la diferencia con el actualCash contado por el cajero.
     *
     * @return CashCloseResponse con expectedCash, actualCash y difference
     */
    @Transactional
    public com.services.crm.models.response.CashCloseResponse closeSession(UUID sessionId, CashCloseRequest request) {
        CashSession session = cashSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException("SESSION_NOT_FOUND", "Sesión de caja no encontrada",
                        org.springframework.http.HttpStatus.NOT_FOUND));

        if (session.getClosedAt() != null) {
            throw new BusinessException("ALREADY_CLOSED", "La sesión ya se encuentra cerrada",
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        session.setClosedAt(OffsetDateTime.now());
        session = cashSessionRepository.save(session);

        // Calcular efectivo esperado: fondo + ventas_cash - gastos_cash -
        // pagos_proveedor_cash
        BigDecimal expectedCash = getAvailableCash(sessionId);
        BigDecimal actualCash = request.actualCash();
        BigDecimal difference = actualCash.subtract(expectedCash);

        return new com.services.crm.models.response.CashCloseResponse(
                session.getId().toString(),
                session.getInitialAmount(),
                session.getTotalSales(),
                session.getCashTotal(),
                session.getCardTotal(),
                session.getTransferTotal(),
                session.getOpenedAt(),
                session.getClosedAt(),
                expectedCash,
                actualCash,
                difference);
    }

    public CashSession getOpenSessionEntity(UUID tenantId) {
        return cashSessionRepository.findByTenantIdAndClosedAtIsNull(tenantId)
                .orElseThrow(() -> new BusinessException("NO_OPEN_SESSION", "No hay una sesión de caja abierta",
                        org.springframework.http.HttpStatus.BAD_REQUEST));
    }
}
