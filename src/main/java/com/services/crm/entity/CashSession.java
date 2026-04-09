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
@Table(name = "cash_sessions", indexes = {
        @Index(name = "idx_cash_sessions_tenant", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashSession {

    /** Unique ID of the cash session (shift) */
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** The store (tenant) where this cash session is being held */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false, updatable = false)
    private Tenant tenant;

    /** The employee (cashier) responsible for this shift */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Starting amount of cash in the drawer (fondo de caja) */
    @Column(name = "initial_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal initialAmount;

    /** Cumulative total of all sales performed during the shift */
    @Column(name = "total_sales", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSales = BigDecimal.ZERO;

    /** Total amount collected in physical cash */
    @Column(name = "cash_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cashTotal = BigDecimal.ZERO;

    /** Total amount collected via credit/debit card */
    @Column(name = "card_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cardTotal = BigDecimal.ZERO;

    /** Total amount collected via bank transfer or digital payments */
    @Column(name = "transfer_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal transferTotal = BigDecimal.ZERO;

    /** Timestamp of the shift opening */
    @Column(name = "opened_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime openedAt = OffsetDateTime.now();

    /** Timestamp of the shift closure (NULL if shift is still active) */
    @Column(name = "closed_at")
    private OffsetDateTime closedAt;
}
