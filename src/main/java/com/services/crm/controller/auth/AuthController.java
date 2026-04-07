package com.services.crm.controller.auth;

import com.services.crm.models.request.LoginRequest;
import com.services.crm.models.request.RegisterTenantRequest;
import com.services.crm.models.response.AuthResponse;
import com.services.crm.service.AuthCRMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCRMService authCRMService;

    @PostMapping("/crm/register")
    public ResponseEntity<AuthResponse> registerTenant(@Valid @RequestBody RegisterTenantRequest request) {
        return ResponseEntity.ok(authCRMService.registerTenant(request));
    }

    @PostMapping("/crm/login")
    public ResponseEntity<AuthResponse> loginCRM(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authCRMService.loginCRM(request));
    }

}
