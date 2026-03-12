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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse", description = "Endpoints para gestionar almacenes")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    @Operation(summary = "Lista todos los almacenes")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un almacen por id")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.findOne(id));
    }

    @PostMapping
    @Operation(summary = "Crea un almacen")
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody WarehouseRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseService.saveWarehouse(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un almacen")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(@PathVariable Long id, @Valid @RequestBody WarehouseRequestDTO dto) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(dto, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un almacen")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
