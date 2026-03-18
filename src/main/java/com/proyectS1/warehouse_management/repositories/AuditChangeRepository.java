package com.proyectS1.warehouse_management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.AuditChange;

@Repository
public interface AuditChangeRepository extends JpaRepository<AuditChange, Long> {
    @EntityGraph(attributePaths = {"actorUser", "affectedEntity"})
    List<AuditChange> findAllByOrderByCreatedAtDesc();
}
