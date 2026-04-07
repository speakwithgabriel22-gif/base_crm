package com.services.crm.service;

import com.services.crm.entity.Tenant;
import com.services.crm.entity.User;
import com.services.crm.enums.UserRole;
import com.services.crm.models.request.LoginRequest;
import com.services.crm.models.request.RegisterTenantRequest;
import com.services.crm.models.response.AuthResponse;
import com.services.crm.repository.TenantRepository;
import com.services.crm.repository.UserRepository;
import com.services.crm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthCRMService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse registerTenant(RegisterTenantRequest request) {
        // 1. Create Tenant
        Tenant tenant = new Tenant();
        tenant.setName(request.getTenantName());
        tenant.setEmail(request.getAdminEmail());
        tenant.setPhone(request.getAdminPhone());
        tenant = tenantRepository.save(tenant);

        // 2. Create Admin User
        User user = new User();
        user.setTenant(tenant);
        user.setFullName(request.getAdminFullName());
        user.setEmail(request.getAdminEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        user.setRole(UserRole.OWNER);
        user = userRepository.save(user);

        // 3. Generate JWT
        String token = jwtUtil.generateTokenForCRM(user.getId(), tenant.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }

    public AuthResponse loginCRM(LoginRequest request) {
        User user = userRepository.findByIdentifierAndActive(request.getIdentifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials or inactive user"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateTokenForCRM(user.getId(), user.getTenant().getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .user(user)
                .build();
    }
}
