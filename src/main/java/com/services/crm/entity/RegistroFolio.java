package com.services.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.UUID;


/**
 * Control de folios por tenant, tipo y fecha.
 * El incremento real se hace mediante SELECT FOR UPDATE + UPDATE en el
 * servicio.
 * Esta entidad permite auditoría y recuperación ante fallos.
 */
@Entity
@Table(name = "registro_folios", uniqueConstraints = {
        @UniqueConstraint(name = "uk_registro_folios_tenant_tipo_fecha", columnNames = { "tenant_id", "tipo", "fecha" })
}, indexes = {
        @Index(name = "idx_folios_tenant_fecha", columnList = "tenant_id, fecha"),
        @Index(name = "idx_folios_tipo", columnList = "tipo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroFolio {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /**
     * Tipo de folio:
     * "SAL" → Ventas (Sales)
     * "ORD" → Pedidos (Orders) - futuro
     * "COT" → Cotizaciones - futuro
     */
    @Column(name = "tipo", nullable = false, length = 3)
    private String tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    /**
     * Último folio usado en la fecha.
     * El siguiente será ultimoFolio + 1
     */
    @Column(name = "ultimo_folio", nullable = false)
    private Integer ultimoFolio;

    /**
     * Método de conveniencia para generar el folio formateado
     * Ejemplo: SAL-20260406-0001
     */
    public String getFolioFormateado() {
        return String.format("%s-%s-%04d",
                tipo,
                fecha.toString().replace("-", ""),
                ultimoFolio);
    }
}