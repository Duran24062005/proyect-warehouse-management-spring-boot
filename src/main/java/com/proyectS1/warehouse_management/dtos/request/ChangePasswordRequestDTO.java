package com.proyectS1.warehouse_management.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequestDTO(
    @NotNull
    @Schema(description = "User id", example = "1")
    Long userId,
    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "Current raw password", example = "123456")
    String currentPassword,
    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "New raw password", example = "654321")
    String newPassword
) {
}
