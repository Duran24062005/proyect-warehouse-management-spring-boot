package com.proyectS1.warehouse_management.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthLoginRequestDTO(
    @Email
    @NotBlank
    @Schema(description = "User email", example = "admin@logitrack.com")
    String email,
    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "Raw password", example = "123456")
    String password
) {
}
