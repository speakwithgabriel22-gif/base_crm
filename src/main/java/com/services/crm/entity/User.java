package com.services.crm.entity;

import com.services.crm.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uq_users_tenant_email", columnNames = {
        "tenant_id", "email" }), indexes = {
                @Index(name = "idx_users_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_users_email", columnList = "email")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Unique ID of the user */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The store this user belongs to */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    /** Mobile phone number used for login or notifications */
    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    /** Email address used for login and identity */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** Quick access PIN for the POS terminal */
    @JsonIgnore
    @Column(length = 6)
    private String pin;

    /** BCrypt hash of the user's password (hidden in JSON) */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** User's formal full name */
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    /** Access level: ADMIN, AGENT, VIEWER, etc. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private UserRole role = UserRole.AGENT;

    /** Whether the user is permitted to log in */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Soft-delete flag (1: active, 0: deleted) */
    @Column(name = "reg_borrado", nullable = false)
    @Builder.Default
    private Integer regBorrado = 1;

    /** Timestamp of the most recent successful authentication */
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    /** Record creation date */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Last record modification date */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
