package com.proyectS1.warehouse_management.dtos.request;

import com.proyectS1.warehouse_management.model.enums.MovementType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MovementRequestDTO(
    @NotNull
    @Schema(description = "Movement type", example = "ENTRY")
    MovementType movementType,
    @Schema(description = "Origin warehouse id", example = "1")
    Long originWarehouseId,
    @Schema(description = "Destination warehouse id", example = "2")
    Long destinationWarehouseId,
    @NotNull
    @Schema(description = "Product id", example = "1")
    Long productId,
    @NotNull
    @Positive
    @Schema(description = "Movement quantity", example = "25")
    Integer quantity
) {
}
