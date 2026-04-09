package com.services.crm.service;

import com.services.crm.entity.Tenant;
import com.services.crm.entity.User;
import com.services.crm.enums.SubscriptionStatus;
import com.services.crm.enums.UserRole;
import com.services.crm.exception.InvalidPinException;
import com.services.crm.exception.ResourceNotFoundException;
import com.services.crm.models.request.LoginRequest;
import com.services.crm.models.request.RegisterRequest;
import com.services.crm.models.response.AuthResponse;
import com.services.crm.entity.UserTenant;
import com.services.crm.repository.TenantRepository;
import com.services.crm.repository.UserRepository;
import com.services.crm.repository.UserTenantRepository;
import com.services.crm.security.JwtUtil;
import com.services.crm.utils.mapper.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Servicio de autenticación para MEYISOFT POS (CRM).
 * Maneja registro de tiendas nuevas y login por PIN de 4 dígitos.
 */
@Service
@RequiredArgsConstructor
public class AuthCRMService {

    /** Repositorio de tiendas para crear/buscar tenant */
    private final TenantRepository tenantRepository;

    /** Repositorio de usuarios para crear/buscar por teléfono */
    private final UserRepository userRepository;

    /** Repositorio de la tabla intermedia y roles */
    private final UserTenantRepository userTenantRepository;

    /** Encoder BCrypt para hashear y validar PINs */
    private final PasswordEncoder passwordEncoder;

    /** Utilidad para generar tokens JWT */
    private final JwtUtil jwtUtil;

    /** Mapper MapStruct para convertir entidades a DTOs */
    private final EntityMapper entityMapper;

    /**
     * Registra una tienda nueva junto con su usuario OWNER.
     * Crea el tenant, el usuario, hashea el PIN con BCrypt y genera el JWT.
     *
     * @param request datos de registro (nombre tienda, teléfono, PIN, etc.)
     * @return AuthResponse con token, usuario y tenant como DTOs
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 0. Validar si el teléfono o correo ya existen
        if (userRepository.existsByPhoneAndRegBorrado(request.phone(), 1)) {
            throw new com.services.crm.exception.BusinessException(
                    com.services.crm.exception.ErrorCodes.AUTH_PHONE_ALREADY_REGISTERED,
                    com.services.crm.exception.ErrorCodes.MSG_AUTH_PHONE_ALREADY_REGISTERED,
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmailAndRegBorrado(request.email(), 1)) {
            throw new com.services.crm.exception.BusinessException(
                    com.services.crm.exception.ErrorCodes.AUTH_EMAIL_ALREADY_REGISTERED,
                    com.services.crm.exception.ErrorCodes.MSG_AUTH_EMAIL_ALREADY_REGISTERED,
                    org.springframework.http.HttpStatus.BAD_REQUEST);
        }

        // 1. Crear la tienda (tenant) con suscripción TRIAL de 15 días
        Tenant tenant = new Tenant();
        tenant.setName(request.storeName());
        tenant.setEmail(request.email());
        tenant.setPhone(request.phone());
        tenant.setSubscriptionStatus(SubscriptionStatus.TRIAL);
        tenant.setTrialEndsAt(OffsetDateTime.now().plusDays(15));
        tenant.setIsVerified(true);
        tenant = tenantRepository.save(tenant);

        // 2. Crear usuario OWNER con PIN hasheado (BCrypt, 4 dígitos)
        User user = new User();
        user.setFullName(request.ownerName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPin(passwordEncoder.encode(request.pin()));
        user.setPasswordHash(passwordEncoder.encode("INITIAL_PWD_CHANGE_ME"));
        user = userRepository.save(user);

        // 3. Crear relación multi-tenant
        UserTenant userTenant = new UserTenant(user, tenant, UserRole.OWNER);
        userTenantRepository.save(userTenant);

        // 4. Generar JWT con userId, tenantId y rol
        String token = jwtUtil.generateTokenForCRM(
                user.getId(), tenant.getId(), UserRole.OWNER.name());

        // 5. Retornar DTO con el tenant recién creado
        return new AuthResponse(
                true,
                token,
                entityMapper.toDto(user),
                entityMapper.toDto(tenant),
                UserRole.OWNER.name());
    }

    /**
     * Autentica un usuario por teléfono + PIN de 4 dígitos.
     * Lanza excepción si las credenciales son inválidas — el controller
     * captura la excepción y retorna ApiResponse.error().
     *
     * @param request datos de login (phone, pin)
     * @return AuthResponse con token, usuario y tenant como DTOs
     * @throws ResourceNotFoundException si el teléfono no existe
     * @throws InvalidPinException       si el PIN no coincide
     */
    public AuthResponse login(LoginRequest request) {

        // 1. Buscar usuario por teléfono — si no existe (login mixto), significa que es
        // nuevo
        java.util.Optional<User> userOpt = userRepository.findByPhone(request.phone());

        if (userOpt.isEmpty()) {
            return new AuthResponse(false, null, null, null, null);
        }

        User user = userOpt.get();

        // 2. Validar PIN con BCrypt — lanza excepción si no coincide
        if (!passwordEncoder.matches(request.pin(), user.getPin())) {
            throw new InvalidPinException("PIN incorrecto");
        }

        // 3. Obtener sucursales ya cargadas desde el usuario (con JOIN FETCH evitamos
        // query extra y N+1)
        java.util.List<UserTenant> userTenants = new java.util.ArrayList<>(user.getUserTenants());

        // Si no tiene sucursales, simular que no está verificado/completado (onboarding
        // pendiente)
        if (userTenants.isEmpty()) {
            return new AuthResponse(false, null, null, null, null);
        }

        // 4. Usar el primer tenant de la lista por defecto para la sesión actual
        UserTenant activeTenant = userTenants.get(0);
        String token = jwtUtil.generateTokenForCRM(
                user.getId(), activeTenant.getTenant().getId(), activeTenant.getRole().name());

        // 5. Retornar DTO con el tenant activo
        return new AuthResponse(
                true,
                token,
                entityMapper.toDto(user),
                entityMapper.toDto(activeTenant.getTenant()),
                activeTenant.getRole().name());
    }
}
