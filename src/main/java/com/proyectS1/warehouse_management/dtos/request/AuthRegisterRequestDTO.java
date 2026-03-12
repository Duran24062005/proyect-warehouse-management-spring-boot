package com.proyectS1.warehouse_management.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequestDTO(
    @Email
    @NotBlank
    @Size(max = 100)
    @Schema(description = "User email", example = "user@logitrack.com")
    String email,
    @NotBlank
    @Size(min = 6, max = 255)
    @Schema(description = "Raw password", example = "123456")
    String password,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "First name", example = "Maria")
    String firstName,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Last name", example = "Lopez")
    String lastName,
    @NotBlank
    @Size(max = 100)
    @Schema(description = "Phone number", example = "3000000002")
    String phoneNumber
) {
}
