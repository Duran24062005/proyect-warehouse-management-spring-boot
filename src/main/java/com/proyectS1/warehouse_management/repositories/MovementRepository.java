package com.proyectS1.warehouse_management.repositories;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectS1.warehouse_management.model.Movement;
import com.proyectS1.warehouse_management.model.enums.MovementType;


@Repository
public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findTop10ByOrderByCreatedAtDescIdDesc();

    long countByMovementType(MovementType movementType);

    List<Movement> findByProductId(Long productId);
    java.util.Optional<Movement> findTopByProductIdOrderByCreatedAtDescIdDesc(Long productId);
    List<Movement> findByOriginWarehouseIdOrDestinationWarehouseId(Long originWarehouseId, Long destinationWarehouseId);
    List<Movement> findByOriginWarehouseIdInOrDestinationWarehouseIdIn(
        Collection<Long> originWarehouseIds,
        Collection<Long> destinationWarehouseIds
    );
    List<Movement> findTop10ByOriginWarehouseIdInOrDestinationWarehouseIdInOrderByCreatedAtDescIdDesc(
        Collection<Long> originWarehouseIds,
        Collection<Long> destinationWarehouseIds
    );
}
