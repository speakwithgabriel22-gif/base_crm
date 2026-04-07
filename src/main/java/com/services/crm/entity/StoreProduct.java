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
@Table(name = "store_products", uniqueConstraints = {
        @UniqueConstraint(name = "idx_store_product_unique", columnNames = { "tenant_id", "upc" })
}, indexes = {
        @Index(name = "idx_store_products_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreProduct {

    /** Unique ID for the local store product record */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The store (tenant) that owns this inventory record */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    /** Reference to the global product catalog data */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upc", nullable = false)
    private UpcCatalog upcCatalog;

    /** The cost price at which the store last purchased this product */
    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    /** The primary supplier for this product for this specific store */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    /** The current selling price set by this store */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** Current physical quantity available in the store */
    @Column(nullable = false)
    private Integer stock;

    /** Threshold for low-stock alerts */
    @Column(name = "min_stock", nullable = false)
    @Builder.Default
    private Integer minStock = 3;

    /** Whether this product is currently visible for sale in the store */
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
