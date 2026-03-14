package com.proyectS1.warehouse_management.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.TrackedEntity;

@Repository
public interface TrackedEntityRepository extends JpaRepository<TrackedEntity, Long> {
    Optional<TrackedEntity> findByName(String name);
}
