package com.services.crm.models.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductRequest(
    @NotBlank(message = "El código de barras es obligatorio")
    String upc,
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    String name,
    
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a cero")
    BigDecimal price,
    
    @NotNull(message = "El stock inicial es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    BigDecimal stock,
    
    @PositiveOrZero(message = "El stock mínimo no puede ser negativo")
    BigDecimal minStock,
    
    BigDecimal costPrice,
    
    java.util.UUID supplierId
) {}
