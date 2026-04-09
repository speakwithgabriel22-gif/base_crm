package com.services.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_suppliers_tenant_id", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {

    /** Unique ID for the supplier record */
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The store (tenant) that manages this supplier */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    /** Legal or trade name of the supplier (e.g., "Bimbo S.A. de C.V.") */
    @Column(nullable = false, length = 150)
    private String name;

    /** Nombre del contacto o preventista del proveedor */
    @Column(name = "contact_name", length = 150)
    private String contactName;

    /** Phone number for the supplier or account representative */
    @Column(length = 20)
    private String phone;

    /** Business category (e.g., "Panadería", "Lácteos") */
    @Column(length = 100)
    private String category;

    /** Days when the account representative visits the store */
    @Column(name = "visit_day", length = 50)
    private String visitDay;

    /** Days when the actual delivery takes place */
    @Column(name = "delivery_day", length = 50)
    private String deliveryDay;

    /** Frequency of visits (e.g., "Semanal", "Quincenal") */
    @Column(length = 30)
    private String frequency;

    /** Total outstanding balance owed to this supplier by the store */
    @Column(name = "total_debt", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalDebt = BigDecimal.ZERO;

    /** Whether this supplier record is active for the store */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Soft-delete flag (1: active, 0: deleted) */
    @Column(name = "reg_borrado", nullable = false)
    @Builder.Default
    private Integer regBorrado = 1;

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
