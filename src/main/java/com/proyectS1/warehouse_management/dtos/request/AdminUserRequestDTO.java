package com.proyectS1.warehouse_management.dtos.request;

import com.proyectS1.warehouse_management.model.enums.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserRequestDTO(
    @Email
    @NotBlank
    @Size(max = 100)
    @Schema(description = "User email", example = "nuevo.admin@logitrack.com")
    String email,
    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "Raw password", example = "Admin123!")
    String password,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "First name", example = "Laura")
    String firstName,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Last name", example = "Suarez")
    String lastName,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Phone number", example = "3000000010")
    String phoneNumber,
    @NotNull
    @Schema(description = "Role", example = "ADMIN")
    UserRole role,
    @Schema(description = "Enabled flag", example = "true")
    Boolean enabled
) {
}
