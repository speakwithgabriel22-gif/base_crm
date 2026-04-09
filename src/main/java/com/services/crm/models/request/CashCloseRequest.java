package com.services.crm.models.request;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CashCloseRequest(
    @NotNull(message = "El monto real en caja es obligatorio")
    @PositiveOrZero(message = "El monto real no puede ser negativo")
    BigDecimal actualCash
) {}
