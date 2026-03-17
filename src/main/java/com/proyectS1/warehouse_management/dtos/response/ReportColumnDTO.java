package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReportColumnDTO(
    @Schema(description = "Column key")
    String key,
    @Schema(description = "Column label")
    String label
) {
}
