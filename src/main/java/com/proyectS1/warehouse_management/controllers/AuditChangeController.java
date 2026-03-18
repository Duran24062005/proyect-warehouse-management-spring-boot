package com.proyectS1.warehouse_management.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.response.AuditChangeResponseDTO;
import com.proyectS1.warehouse_management.model.enums.OperationType;
import com.proyectS1.warehouse_management.services.AuditChangeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/audit-changes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Audit", description = "Endpoints para consultar historial de auditoria")
public class AuditChangeController {

    private final AuditChangeService auditChangeService;

    @GetMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Audit history successfully obtained"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Lista historial de auditoria", description = "Permite filtrar por operacion, entidad, actor y bodega")
    public ResponseEntity<List<AuditChangeResponseDTO>> getAuditChanges(
        @RequestParam(required = false) OperationType operationType,
        @RequestParam(required = false) String entityName,
        @RequestParam(required = false) Long actorUserId,
        @RequestParam(required = false) Long warehouseId
    ) {
        return ResponseEntity.ok(
            auditChangeService.findVisibleAuditChanges(operationType, entityName, actorUserId, warehouseId)
        );
    }
}
