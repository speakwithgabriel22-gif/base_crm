package com.services.crm.repository;

import com.services.crm.entity.CashSession;
import com.services.crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing cashier shifts.
 */
@Repository
public interface CashSessionRepository extends JpaRepository<CashSession, UUID> {

    /**
     * Finds the current open session for a specific user in a specific tenant.
     * @param tenantId The store ID.
     * @param user The user (cashier).
     * @return Optional containing the open session if exists.
     */
    Optional<CashSession> findFirstByTenantIdAndUserAndClosedAtIsNull(UUID tenantId, User user);
}
