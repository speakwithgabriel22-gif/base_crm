package com.services.crm.models.response;

public record InitResponse(
    UserDTO user,
    TenantDTO tenant,
    String role
) {}
