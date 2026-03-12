package com.proyectS1.warehouse_management.dtos.response;

import java.time.LocalDateTime;

import com.proyectS1.warehouse_management.model.enums.MovementType;

import io.swagger.v3.oas.annotations.media.Schema;

public record MovementResponseDTO(
    @Schema(description = "Unique identifier")
    Long id,
    @Schema(description = "Movement type")
    MovementType movementType,
    @Schema(description = "Employee user id")
    Long employeeUserId,
    @Schema(description = "Employee full name")
    String employeeName,
    @Schema(description = "Origin warehouse id")
    Long originWarehouseId,
    @Schema(description = "Origin warehouse name")
    String originWarehouseName,
    @Schema(description = "Destination warehouse id")
    Long destinationWarehouseId,
    @Schema(description = "Destination warehouse name")
    String destinationWarehouseName,
    @Schema(description = "Product id")
    Long productId,
    @Schema(description = "Product name")
    String productName,
    @Schema(description = "Quantity")
    Integer quantity,
    @Schema(description = "Creation date")
    LocalDateTime createdAt
) {
}
