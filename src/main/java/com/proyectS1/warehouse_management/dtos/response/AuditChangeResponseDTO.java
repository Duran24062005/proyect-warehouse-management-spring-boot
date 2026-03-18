package com.proyectS1.warehouse_management.dtos.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.proyectS1.warehouse_management.model.enums.OperationType;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuditChangeResponseDTO(
    @Schema(description = "Audit change identifier")
    Long id,
    @Schema(description = "Audit creation timestamp")
    LocalDateTime createdAt,
    @Schema(description = "Operation type")
    OperationType operationType,
    @Schema(description = "Affected entity technical name")
    String entityName,
    @Schema(description = "Affected entity description")
    String entityDescription,
    @Schema(description = "Actor user identifier")
    Long actorUserId,
    @Schema(description = "Actor user display name")
    String actorUserName,
    @Schema(description = "Actor user email")
    String actorUserEmail,
    @Schema(description = "Previous values snapshot")
    Map<String, Object> oldValues,
    @Schema(description = "New values snapshot")
    Map<String, Object> newValues
) {
}
