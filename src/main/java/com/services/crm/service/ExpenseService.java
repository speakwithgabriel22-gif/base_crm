package com.services.crm.service;

import com.services.crm.entity.CashSession;
import com.services.crm.entity.Expense;
import com.services.crm.entity.Tenant;
import com.services.crm.exception.BusinessException;
import com.services.crm.exception.InsufficientCashException;
import com.services.crm.models.request.ExpenseRequest;
import com.services.crm.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.services.crm.enums.PaymentType;

import java.math.BigDecimal;
import java.util.UUID;

import com.services.crm.models.response.ExpenseDTO;
import com.services.crm.utils.mapper.EntityMapper;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CashService cashService;
    private final EntityMapper entityMapper;

    @Transactional
    public ExpenseDTO create(Tenant tenant, ExpenseRequest request) {
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "El monto del gasto debe ser mayor a cero",
                    HttpStatus.BAD_REQUEST);
        }

        // En el POS, los gastos suelen ser en efectivo desde la caja
        BigDecimal availableCash = cashService.getAvailableCash(request.cashSessionId());

        if (availableCash.compareTo(request.amount()) < 0) {
            throw new InsufficientCashException("No hay suficiente efectivo en caja para cubrir este gasto");
        }

        CashSession session = cashService.getOpenSessionEntity(tenant.getId());
        if (!session.getId().equals(request.cashSessionId())) {
            throw new BusinessException("INVALID_SESSION", "La sesión de caja no coincide con la abierta",
                    HttpStatus.BAD_REQUEST);
        }

        Expense expense = Expense.builder()
                .tenant(tenant)
                .cashSession(session)
                .category(request.category())
                .description(request.description())
                .amount(request.amount())
                .paymentMethod(request.paymentMethod() == null ? PaymentType.CASH : request.paymentMethod())
                .note(request.note())
                .build();

        return entityMapper.toDto(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(UUID expenseId, UUID tenantId) {
        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getTenant().getId().equals(tenantId))
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Gasto no encontrado", HttpStatus.NOT_FOUND));

        expense.setRegBorrado(0);
        expense.setUpdatedAt(OffsetDateTime.now());
        expenseRepository.save(expense);
    }
}
