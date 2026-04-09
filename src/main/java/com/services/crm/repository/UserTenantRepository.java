package com.services.crm.repository;

import com.services.crm.entity.UserTenant;
import com.services.crm.entity.UserTenantId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTenantRepository extends JpaRepository<UserTenant, UserTenantId> {

    @EntityGraph(attributePaths = {"tenant"})
    List<UserTenant> findByUserId(UUID userId);
}
