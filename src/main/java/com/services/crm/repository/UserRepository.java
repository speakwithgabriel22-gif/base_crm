package com.services.crm.repository;

import com.services.crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de usuarios.
 * Todas las consultas que necesitan el tenant usan JOIN FETCH para evitar N+1.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Busca usuario activo por email o teléfono con su tenant precargado.
     * JOIN FETCH evita una segunda consulta al acceder a user.getTenant().
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userTenants ut " +
           "LEFT JOIN FETCH ut.tenant " +
           "WHERE (u.email = :identifier OR u.phone = :identifier) " +
           "AND u.isActive = true AND u.regBorrado = 1")
    Optional<User> findByIdentifierAndActive(@Param("identifier") String identifier);

    /**
     * Busca usuario activo por teléfono con su tenant precargado.
     * Se usa en login — JOIN FETCH crítico para evitar N+1 en auth.
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userTenants ut " +
           "LEFT JOIN FETCH ut.tenant " +
           "WHERE u.phone = :phone AND u.isActive = true AND u.regBorrado = 1")
    Optional<User> findByPhone(@Param("phone") String phone);

    boolean existsByPhoneAndRegBorrado(String phone, Integer regBorrado);
    boolean existsByEmailAndRegBorrado(String email, Integer regBorrado);
}
