package com.proyectS1.warehouse_management.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthLoginResponseDTO(
    @Schema(description = "Result message")
    String message,
    @Schema(description = "Bearer token type")
    String tokenType,
    @Schema(description = "JWT access token")
    String accessToken,
    @Schema(description = "Authenticated user")
    UserResponseDTO user
) {
}
