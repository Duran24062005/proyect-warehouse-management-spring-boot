package com.proyectS1.warehouse_management.dtos.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WarehouseRequestDTO(
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Warehouse name", example = "Bodega Central Bogota")
    String name,
    @NotBlank
    @Size(max = 255)
    @Schema(description = "Warehouse location", example = "Bogota, DC")
    String ubication,
    @DecimalMin(value = "0.0", inclusive = true)
    @Schema(description = "Maximum warehouse capacity", example = "5000.000")
    BigDecimal capacity,
    @Schema(description = "Manager user id", example = "2")
    Long managerUserId
) {
}
