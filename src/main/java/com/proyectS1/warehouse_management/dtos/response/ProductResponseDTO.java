package com.proyectS1.warehouse_management.dtos.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record ProductResponseDTO(
    @Schema(description="Unic identifier")
    Long id,
    @Schema(description="Product description")
    String name,
    @Schema(description="Product category")
    String category,
    @Schema(description="Price")
    BigDecimal price,
    @Schema(description="Warehouse id")
    Long warehouseId,
    @Schema(description="Warehouse name")
    String warehouseName
) {
}
