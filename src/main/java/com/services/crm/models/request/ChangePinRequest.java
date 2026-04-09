package com.services.crm.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePinRequest(
    @NotBlank(message = "El PIN antiguo es obligatorio")
    String oldPin,

    @NotBlank(message = "El PIN nuevo es obligatorio")
    @Size(min = 4, max = 4, message = "El PIN debe ser de 4 dígitos")
    String newPin
) {}
