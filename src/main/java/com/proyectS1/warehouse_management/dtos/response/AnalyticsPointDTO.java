package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnalyticsPointDTO(
    @Schema(description = "Point time in YYYY-MM-DD format")
    String time,
    @Schema(description = "Aggregated numeric value")
    Integer value
) {
}
