package com.proyectS1.warehouse_management.dtos.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

public record WarehouseResponseDTO(
    @Schema(description = "Unique identifier")
    Long id,
    @Schema(description = "Warehouse name")
    String name,
    @Schema(description = "Warehouse location")
    String ubication,
    @Schema(description = "Maximum capacity")
    BigDecimal capacity,
    @Schema(description = "Manager user id")
    Long managerUserId,
    @Schema(description = "Manager full name")
    String managerName
) {
}
