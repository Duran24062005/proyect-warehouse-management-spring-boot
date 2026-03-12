package com.proyectS1.warehouse_management.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.Movement;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByProductId(Long productId);
    List<Movement> findByOriginWarehouseIdOrDestinationWarehouseId(Long originWarehouseId, Long destinationWarehouseId);
}
