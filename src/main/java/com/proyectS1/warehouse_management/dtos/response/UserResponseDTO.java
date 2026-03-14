package com.proyectS1.warehouse_management.dtos.response;

import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponseDTO(
    @Schema(description = "Unique identifier")
    Long id,
    @Schema(description = "Email")
    String email,
    @Schema(description = "First name")
    String firstName,
    @Schema(description = "Last name")
    String lastName,
    @Schema(description = "Phone number")
    String phoneNumber,
    @Schema(description = "Role")
    UserRole role,
    @Schema(description = "Enabled status")
    Boolean enabled,
    @Schema(description="User status")
    UserStatus userStatus
) {
}
