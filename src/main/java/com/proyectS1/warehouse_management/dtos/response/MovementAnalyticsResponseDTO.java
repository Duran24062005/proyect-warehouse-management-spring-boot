package com.proyectS1.warehouse_management.dtos.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

public record MovementAnalyticsResponseDTO(
    @Schema(description = "Analytics title")
    String title,
    @Schema(description = "Analytics subtitle")
    String subtitle,
    @Schema(description = "Window token")
    String window,
    @Schema(description = "Range label")
    String rangeLabel,
    @Schema(description = "Generation date")
    LocalDateTime generatedAt,
    @Schema(description = "Applied filters")
    Map<String, String> filters,
    @Schema(description = "Summary metrics")
    List<ReportSummaryItemDTO> summary,
    @Schema(description = "Chart series")
    List<AnalyticsSeriesDTO> series,
    @Schema(description = "Primary points")
    List<AnalyticsPointDTO> points
) {
}
