package com.services.crm.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.services.crm.enums.MeasurementUnit;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "upc_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcCatalog {

    @Id
    @Column(length = 20, nullable = false, updatable = false)
    private String upc;

    @Column(nullable = false, length = 500)
    private String nombre;

    @Column(length = 100)
    private String marca;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private MeasurementUnit measurementUnit = MeasurementUnit.PZA;
}