package com.proyectS1.warehouse_management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
