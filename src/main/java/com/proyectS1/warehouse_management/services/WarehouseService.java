package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.request.WarehouseRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.WarehouseResponseDTO;

public interface WarehouseService {
    WarehouseResponseDTO saveWarehouse(WarehouseRequestDTO dto);
    WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO dto, Long id);
    void deleteWarehouse(Long id);
    List<WarehouseResponseDTO> findAll();
    List<WarehouseResponseDTO> findAllForReferences();
    WarehouseResponseDTO findOne(Long id);
}
