package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.request.MovementRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;

public interface MovementService {
    MovementResponseDTO saveMovement(MovementRequestDTO dto);
    MovementResponseDTO updateMovement(MovementRequestDTO dto, Long id);
    void deleteMovement(Long id);
    List<MovementResponseDTO> findAll();
    MovementResponseDTO findOne(Long id);
    List<MovementResponseDTO> findByProduct(Long productId);
    List<MovementResponseDTO> findByWarehouse(Long warehouseId);
}
