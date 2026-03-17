package com.proyectS1.warehouse_management.dtos.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record AnalyticsSeriesDTO(
    @Schema(description = "Series identifier")
    String id,
    @Schema(description = "Series label")
    String label,
    @Schema(description = "Series color")
    String color,
    @Schema(description = "Series data points")
    List<AnalyticsPointDTO> data
) {
}
