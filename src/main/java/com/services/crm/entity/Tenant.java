package com.services.crm.entity;

import com.services.crm.enums.SubscriptionStatus;
import com.services.crm.enums.TenantPlan;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenants_is_active", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    /** Unique ID of the tenant (Store) */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Legal or commercial name of the store */
    @Column(nullable = false, length = 255)
    private String name;

    /** Owner's email address, used for login and notifications */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** Contact phone number of the store */
    @Column(length = 30)
    private String phone;

    /** Physical address of the store */
    @Column(length = 255)
    private String address;

    /** Verification status (e.g., identity or phone verification) */
    @Column(name = "is_verified")
    private Boolean isVerified;

    /** Current subscription lifecycle status: TRIAL, ACTIVE, EXPIRED, CANCELLED */
    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 30)
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    /** Timestamp when the free trial began */
    @Column(name = "trial_started_at")
    @Builder.Default
    private OffsetDateTime trialStartedAt = OffsetDateTime.now();

    /** Timestamp when the free trial should transition to paid or expired */
    @Column(name = "trial_ends_at")
    @Builder.Default
    private OffsetDateTime trialEndsAt = OffsetDateTime.now().plusDays(15);

    /** Timestamp when a specific plan was last selected or renewed */
    @Column(name = "plan_selected_at")
    private OffsetDateTime planSelectedAt;

    /** The current service tier: STARTER, PRO, ENTERPRISE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TenantPlan plan = TenantPlan.STARTER;

    /** Global flag to enable/disable access to this store */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Soft delete flag (1: active, 0: deleted) */
    @Column(name = "reg_borrado", nullable = false)
    @Builder.Default
    private Integer regBorrado = 1;

    /** General store settings (logo, color theme, etc.) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> settings = Map.of();

    /** Relación a los múltiples usuarios que tiene la sucursal */
    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserTenant> userTenants = new HashSet<>();

    /** Record creation date */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Last record update date */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}