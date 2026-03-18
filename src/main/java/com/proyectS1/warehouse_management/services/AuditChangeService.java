package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.response.AuditChangeResponseDTO;
import com.proyectS1.warehouse_management.model.enums.OperationType;

public interface AuditChangeService {
    List<AuditChangeResponseDTO> findVisibleAuditChanges(
        OperationType operationType,
        String entityName,
        Long actorUserId,
        Long warehouseId
    );
}
