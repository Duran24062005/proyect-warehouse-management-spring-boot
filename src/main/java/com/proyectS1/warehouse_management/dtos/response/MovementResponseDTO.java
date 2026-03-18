package com.proyectS1.warehouse_management.dtos.response;

import java.time.LocalDateTime;

import com.proyectS1.warehouse_management.model.enums.MovementType;

import io.swagger.v3.oas.annotations.media.Schema;

public record MovementResponseDTO(
    @Schema(description = "Unique identifier")
    Long id,
    @Schema(description = "Movement type")
    MovementType movementType,
    @Schema(description = "User id that registered the movement")
    Long registeredByUserId,
    @Schema(description = "Full name of the user that registered the movement")
    String registeredByName,
    @Schema(description = "Employee id that performed the movement")
    Long performedByEmployeeId,
    @Schema(description = "Full name of the employee that performed the movement")
    String performedByEmployeeName,
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
    @Schema(description = "Creation date")
    LocalDateTime createdAt
) {
}
