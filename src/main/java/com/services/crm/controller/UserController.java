package com.services.crm.controller;

import com.services.crm.entity.User;
import com.services.crm.exception.InvalidPinException;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.ChangePinRequest;
import com.services.crm.models.response.ApiResponse;
import com.services.crm.repository.UserRepository;
import com.services.crm.security.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.services.crm.entity.UserTenant;
import com.services.crm.models.response.InitResponse;
import com.services.crm.utils.mapper.EntityMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 👤 Usuario — Seguridad y configuración personal.
 *
 * Permite al usuario autenticado gestionar su seguridad:
 * cambio de PIN de acceso.
 *
 * El PIN es de 4 dígitos y se hashea con BCrypt.
 * NUNCA se almacena en texto plano.
 *
 * Base path: /api/v1/user
 * Autenticación: JWT requerido (OWNER o AGENT — cada uno cambia su propio PIN)
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    /** Repositorio de usuarios con JOIN FETCH para cargar tenant */
    private final UserRepository userRepository;

    /** Encoder BCrypt para validar PIN actual y hashear el nuevo */
    private final PasswordEncoder passwordEncoder;

    /** Helper para extraer userId del JWT */
    private final AuthContext authContext;

    /**
     * POST /api/v1/user/change-pin
     *
     * Cambiar el PIN de acceso del usuario autenticado.
     *
     * Flujo:
     * 1. Valida que el PIN actual (oldPin) coincida con el hasheado en BD
     * 2. Hashea el nuevo PIN (newPin) con BCrypt
     * 3. Persiste el cambio
     *
     * Validaciones:
     * - oldPin: obligatorio, se compara con BCrypt
     * - newPin: obligatorio, exactamente 4 dígitos
     *
     * @param request ChangePinRequest con PIN actual y nuevo
     * @return ApiResponse<String> confirmación del cambio
     * @throws InvalidPinException                                  si el PIN actual
     *                                                              no coincide
     * @throws com.services.crm.exception.ResourceNotFoundException si el usuario no
     *                                                              existe
     */
    @PostMapping("/change-pin")
    public ResponseEntity<ApiResponse<String>> changePin(
            @Valid @RequestBody ChangePinRequest request) {

        // Obtener usuario autenticado
        User user = userRepository.findById(authContext.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validar PIN actual con BCrypt
        if (!passwordEncoder.matches(request.oldPin(), user.getPin())) {
            throw new InvalidPinException("El PIN actual es incorrecto");
        }

        // Hashear y guardar nuevo PIN
        user.setPin(passwordEncoder.encode(request.newPin()));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("PIN actualizado correctamente"));
    }

    /**
     * GET /api/v1/user/init
     *
     * Endpoint rápido de inicialización. Retorna la información fresca del usuario,
     * incluyendo el estatus de emailVerified y su branch activo para mapear
     * la redirección de pantallas en el Frontend.
     */
    @GetMapping("/init")
    public ResponseEntity<ApiResponse<InitResponse>> init() {
        User user = userRepository.findById(authContext.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        java.util.List<UserTenant> userTenants = new java.util.ArrayList<>(user.getUserTenants());

        // Si no tiene sucursal, retorna los puros datos personales
        if (userTenants.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(new InitResponse(
                    EntityMapper.INSTANCE.toDto(user),
                    null,
                    null)));
        }

        UserTenant activeTenant = userTenants.get(0);

        return ResponseEntity.ok(ApiResponse.success(new InitResponse(
                EntityMapper.INSTANCE.toDto(user),
                EntityMapper.INSTANCE.toDto(activeTenant.getTenant()),
                activeTenant.getRole().name())));
    }
}
