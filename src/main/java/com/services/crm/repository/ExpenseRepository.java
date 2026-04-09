package com.services.crm.repository;

import com.services.crm.entity.Expense;
import com.services.crm.models.response.ExpenseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Repositorio de gastos.
 * Proyección JPQL para lecturas, entidad solo para escrituras.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /**
     * Lista gastos de una sesión de caja — proyección directa a ExpenseDTO.
     * Solo trae: id, cashSessionId, category, amount, note, paymentMethod, createdAt.
     * No carga: tenant, entidad completa, timestamps de update, regBorrado.
     */
    @Query("SELECT new com.services.crm.models.response.ExpenseDTO(" +
           "CAST(e.id AS string), CAST(e.cashSession.id AS string), " +
           "e.category, e.description, e.amount, e.note, e.paymentMethod, e.createdAt) " +
           "FROM Expense e WHERE e.cashSession.id = :cashSessionId AND e.regBorrado = 1 " +
           "ORDER BY e.createdAt DESC")
    List<ExpenseDTO> findAllDtoByCashSessionId(@Param("cashSessionId") UUID cashSessionId);

    /**
     * Suma total de gastos en EFECTIVO de una sesión de caja.
     * COALESCE evita null — la fórmula de efectivo disponible lo necesita.
     * Solo suma el campo amount, no carga entidades.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.cashSession.id = :cashSessionId " +
           "AND e.paymentMethod = 'CASH' AND e.regBorrado = 1")
    BigDecimal sumCashExpensesByCashSessionId(@Param("cashSessionId") UUID cashSessionId);
}
