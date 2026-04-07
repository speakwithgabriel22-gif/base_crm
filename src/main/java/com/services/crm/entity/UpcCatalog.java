package com.services.crm.entity;

import com.services.crm.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "upc_catalog", indexes = {
        @Index(name = "idx_upc_catalog_upc", columnList = "upc")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcCatalog {

    /** Unique barcode for the product (EAN-13, UPC-A, etc.) */
    @Id
    @Column(length = 14, nullable = false)
    private String upc;

    /** Common name of the product (e.g., "Coca Cola 600ml") */
    @Column(nullable = false, length = 200)
    private String nombre;

    /** Brand or manufacturer (e.g., "Bimbo", "Lala") */
    @Column(length = 100)
    private String marca;

    /** Unit of measure (Piece, Kg, Lt, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 20)
    @Builder.Default
    private MeasurementUnit measurementUnit = MeasurementUnit.PZA;

    /** Whether the product is currently active in the global catalog */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /** Soft-delete flag (1: active, 0: deleted) */
    @Column(name = "reg_borrado", nullable = false)
    @Builder.Default
    private Integer regBorrado = 1;

    /** Record creation timestamp */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Record last update timestamp */
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
