package com.services.crm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "users", indexes = {
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

    /** Relación a las múltiples sucursales que tiene el usuario */
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserTenant> userTenants = new HashSet<>();

    /** Mobile phone number used for login or notifications */
    @Column(nullable = false, length = 20, unique = true)
    private String phone;

    /** Email address used for login and identity */
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    /** Indica si el usuario ha verificado su cuenta de correo */
    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    /** Quick access PIN for the POS terminal */
    @JsonIgnore
    @Column(length = 60)
    private String pin;

    /** BCrypt hash of the user's password (hidden in JSON) */
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** User's formal full name */
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

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
