package com.services.crm.controller.auth;

import com.services.crm.models.response.ApiResponse;
import com.services.crm.models.response.AuthResponse;
import com.services.crm.service.AuthCRMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller público de autenticación.
 * Todos los endpoints retornan ApiResponse<AuthResponse> unificado.
 */
@RestController
@RequestMapping("/api/v1/public/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCRMService authCRMService;

    /**
     * Registra tienda nueva + usuario OWNER.
     * Retorna token JWT + datos del usuario y tenant en formato unificado.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody com.services.crm.models.request.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authCRMService.register(request)));
    }

    /**
     * Login por teléfono + PIN de 4 dígitos.
     * Lanza excepción si credenciales inválidas — GlobalExceptionHandler la
     * captura.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody com.services.crm.models.request.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success(authCRMService.login(request)));
    }
}
