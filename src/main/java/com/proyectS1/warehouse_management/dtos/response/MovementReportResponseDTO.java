package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MovementReportResponseDTO(
    @Schema(description="number of recorded movements")
    int totalMovements,
    @Schema(description="number total of entries")
    int totalEntry,
    @Schema(description="number total of exits")
    int totalExit,
    @Schema(description="number total of transfers")
    int totalTransfers
) {
    
}
