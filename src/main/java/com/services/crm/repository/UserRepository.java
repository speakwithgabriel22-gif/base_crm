package com.services.crm.repository;

import com.services.crm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE (u.email = :identifier OR u.phone = :identifier) "
            + "AND u.isActive = true AND u.regBorrado = 1")
    Optional<User> findByIdentifierAndActive(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true AND u.regBorrado = 1")
    Optional<User> findByEmail(@Param("email") String email);
}
