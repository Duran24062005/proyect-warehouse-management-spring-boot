package com.proyectS1.warehouse_management.dtos.request;

import com.proyectS1.warehouse_management.model.enums.MovementType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MovementRequestDTO(
    @NotNull
    @Schema(description = "Movement type", example = "ENTRY")
    MovementType movementType,
    @NotNull(message = "performedByEmployeeId is required")
    @Schema(
        description = "Employee id that physically performed the movement. This field is required for every movement.",
        example = "5",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long performedByEmployeeId,
    @Schema(description = "Origin warehouse id", example = "1")
    Long originWarehouseId,
    @Schema(description = "Destination warehouse id", example = "2")
    Long destinationWarehouseId,
    @NotNull
    @Schema(description = "Product id", example = "1")
    Long productId
) {
}
