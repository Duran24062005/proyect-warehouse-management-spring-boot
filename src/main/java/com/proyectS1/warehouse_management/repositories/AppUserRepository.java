package com.proyectS1.warehouse_management.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.enums.UserRole;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    List<AppUser> findByRole(UserRole role);
}
