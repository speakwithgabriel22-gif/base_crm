package com.services.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "reception_items", indexes = {
        @Index(name = "idx_reception_items_transaction", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceptionItem {

    /** Unique ID of the reception item record */
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The supplier transaction this item details */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private SupplierTransaction transaction;

    /** Reference to the global product item received */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "upc", nullable = false)
    private UpcCatalog upcCatalog;

    /** Quantity of the product received */
    @Column(nullable = false)
    private Integer quantity;

    /** Individual unit cost reported by the supplier today */
    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    /** Calculated line total (quantity * costPrice) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
