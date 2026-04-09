package com.services.crm.models.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "El nombre de la tienda es obligatorio")
    String storeName,

    @NotBlank(message = "El nombre del dueño es obligatorio")
    String ownerName,

    @NotBlank(message = "El teléfono es obligatorio")
    String phone,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String email,

    @NotBlank(message = "El PIN es obligatorio")
    @Size(min = 4, max = 4, message = "El PIN debe ser de 4 dígitos")
    String pin
) {}
