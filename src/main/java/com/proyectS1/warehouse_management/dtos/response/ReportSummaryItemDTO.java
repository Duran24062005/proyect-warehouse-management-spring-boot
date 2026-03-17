package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportSummaryItemDTO(
    @Schema(description = "Summary key")
    String key,
    @Schema(description = "Summary label")
    String label,
    @Schema(description = "Summary value")
    String value
) {
}
