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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movements")
@RequiredArgsConstructor
@Tag(name = "Movement", description = "Endpoints para gestionar movimientos de inventario")
public class MovementController {

    private final MovementService movementService;

    @GetMapping
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
    @Operation(summary = "Obtiene un movimiento por id")
    public ResponseEntity<MovementResponseDTO> getMovementById(@PathVariable Long id) {
        return ResponseEntity.ok(movementService.findOne(id));
    }

    @PostMapping
    @Operation(summary = "Crea un movimiento")
    public ResponseEntity<MovementResponseDTO> createMovement(@Valid @RequestBody MovementRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movementService.saveMovement(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un movimiento")
    public ResponseEntity<MovementResponseDTO> updateMovement(@PathVariable Long id, @Valid @RequestBody MovementRequestDTO dto) {
        return ResponseEntity.ok(movementService.updateMovement(dto, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un movimiento")
    public ResponseEntity<Void> deleteMovement(@PathVariable Long id) {
        movementService.deleteMovement(id);
        return ResponseEntity.noContent().build();
    }
}
