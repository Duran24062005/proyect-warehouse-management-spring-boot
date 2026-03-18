package com.proyectS1.warehouse_management.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.request.MovementRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;
import com.proyectS1.warehouse_management.services.MovementService;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Movement", description = "Endpoints para gestionar movimientos de inventario")
public class MovementController {

    private final MovementService movementService;

    @GetMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Movements successfully obtained"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        }
    )
    @Operation(summary = "Lista movimientos", description = "Permite filtrar por productId o warehouseId")
    public ResponseEntity<List<MovementResponseDTO>> getAllMovements(
        @RequestParam(required = false) Long productId,
        @RequestParam(required = false) Long warehouseId
    ) {
        if (productId != null) {
            return ResponseEntity.ok(movementService.findByProduct(productId));
        }

        if (warehouseId != null) {
            return ResponseEntity.ok(movementService.findByWarehouse(warehouseId));
        }

        return ResponseEntity.ok(movementService.findAll());
    }

    @GetMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Movement successfully obtained"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Movement not found")
        }
    )
    @Operation(summary = "Obtiene un movimiento por id")
    public ResponseEntity<MovementResponseDTO> getMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(movementService.findOne(id));
    }

    @PostMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Movement successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Related warehouse or product not found")
        }
    )
    @Operation(
        summary = "Crea un movimiento",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "The employee executing the movement must be sent in performedByEmployeeId.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "entryMovement",
                    value = """
                        {
                          "movementType": "ENTRY",
                          "performedByEmployeeId": 5,
                          "destinationWarehouseId": 2,
                          "productId": 1
                        }
                        """
                )
            )
        )
    )
    public ResponseEntity<MovementResponseDTO> createMovement(@Valid @RequestBody MovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.saveMovement(dto));
    }

    @PutMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Movement successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Movement or related data not found")
        }
    )
    @Operation(
        summary = "Actualiza un movimiento",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "The employee executing the movement must be sent in performedByEmployeeId.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "updateMovement",
                    value = """
                        {
                          "movementType": "TRANSFER",
                          "performedByEmployeeId": 5,
                          "originWarehouseId": 1,
                          "destinationWarehouseId": 2,
                          "productId": 1
                        }
                        """
                )
            )
        )
    )
    public ResponseEntity<MovementResponseDTO> updateMovement(@PathVariable Long id, @Valid @RequestBody MovementRequestDTO dto) {
        return ResponseEntity.ok(movementService.updateMovement(dto, id));
    }

    @DeleteMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Movement successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Movement not found")
        }
    )
    @Operation(summary = "Elimina un movimiento")
    public ResponseEntity<Void> deleteMovement(@PathVariable Long id) {
        movementService.deleteMovement(id);
        return ResponseEntity.noContent().build();
    }
}
