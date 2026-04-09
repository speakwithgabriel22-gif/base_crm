package com.services.crm.models.response;

/**
 * DTO de usuario para la API.
 * IDs como String (regla AGENTS.md) para evitar acoplamiento con UUID interno.
 *
 * @param id       identificador único del usuario (String, no UUID)
 * @param phone    teléfono del usuario (se usa como login)
 * @param email    correo electrónico
 * @param fullName nombre completo del empleado
 */
public record UserDTO(
        String id,
        String phone,
        String email,
        String fullName,
        boolean emailVerified) {
}
