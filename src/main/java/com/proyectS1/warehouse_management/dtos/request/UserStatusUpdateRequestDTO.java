package com.proyectS1.warehouse_management.dtos.request;

import com.proyectS1.warehouse_management.model.enums.UserStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequestDTO(
    @NotNull
    @Schema(description = "Nuevo estado del usuario", example = "ACTIVE")
    UserStatus status
) {
}
