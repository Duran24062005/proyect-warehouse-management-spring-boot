package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MessageResponseDTO(
    @Schema(description = "Operation result")
    String message
) {
}
