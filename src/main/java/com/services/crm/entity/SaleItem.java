package com.services.crm.entity;

import com.services.crm.enums.MeasurementUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {

    /** Unique ID of the sale item record */
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The sale transaction header this item belongs to */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    /** Reference to the global product sold */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_product_id", nullable = false)
    private StoreProduct storeProduct;

    /** Snapshot of the product name at the moment of sale */
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    /** Snapshot of the measurement unit (Piece, Kg, etc.) at the moment of sale */
    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_unit", nullable = false, length = 20)
    private MeasurementUnit measurementUnit;

    /** Total IVA amount calculated for this line item */
    @Column(name = "iva_amount", precision = 12, scale = 2)
    private BigDecimal ivaAmount;

    /** Total IEPS amount calculated for this line item */
    @Column(name = "ieps_amount", precision = 12, scale = 2)
    private BigDecimal iepsAmount;

    /** Quantity sold (can represent weight or units) */
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    /** Snapshot of the unit price at the moment of sale */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Line subtotal (quantity * unitPrice) before or after taxes depending on
     * settings
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
