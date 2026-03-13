package com.proyectS1.warehouse_management.dtos.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductResponseDTO(
    @Schema(description="Unic identifier")
    Long id,
    @Schema(description="Product description")
    String name,
    @Schema(description="Product category")
    String category,
    @Schema(description="Price")
    BigDecimal price,
    @NotBlank
    @Size(max = 120)
    @Schema(description="Created date")
    LocalDateTime createdAt,
    @NotBlank
    @Schema(description="Updated date")
    LocalDateTime updatedAt,
    @Schema(description="Warehouse id")
    Long warehouseId,
    @Schema(description="Warehouse name")
    String warehouseName
) {
}
