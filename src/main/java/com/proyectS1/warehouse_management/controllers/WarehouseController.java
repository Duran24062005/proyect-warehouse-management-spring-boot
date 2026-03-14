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
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.request.WarehouseRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.WarehouseResponseDTO;
import com.proyectS1.warehouse_management.services.WarehouseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Warehouse", description = "Endpoints para gestionar almacenes")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Warehouses successfully obtained"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        }
    )
    @Operation(summary = "Lista todos los almacenes")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.findAll());
    }

    @GetMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Warehouse successfully obtained"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
        }
    )
    @Operation(summary = "Obtiene un almacen por id")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.findOne(id));
    }

    @PostMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "Warehouse successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Manager user not found")
        }
    )
    @Operation(summary = "Crea un almacen")
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody WarehouseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.saveWarehouse(dto));
    }

    @PutMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Warehouse successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Warehouse or manager user not found")
        }
    )
    @Operation(summary = "Actualiza un almacen")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseRequestDTO dto) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(dto, id));
    }

    @DeleteMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "204", description = "Warehouse successfully deleted"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Warehouse not found")
        }
    )
    @Operation(summary = "Elimina un almacen")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
