package com.proyectS1.warehouse_management.dtos.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductRequestDTO(
    @NotBlank
    @Size(max = 120)
    @Schema(description="Name")
    String name,
    @NotBlank
    @Size(max = 120)
    @Schema(description="Category")
    String category,
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description="Price")
    BigDecimal price,
    @Schema(description="Warehouse id", example = "1")
    Long warehouseId
) {
    
}
