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

import com.proyectS1.warehouse_management.dtos.request.ProductRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.ProductResponseDTO;
import com.proyectS1.warehouse_management.services.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name="Product", description="Endpoint for process products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Products successfully obtained"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Unvalid data / poorly structured body"
                    )
            }
    )
    @Operation(summary = "Lista todos los productos",description = "permite listar todos los productos añadiendo como endpoint (GET)")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){
        return ResponseEntity.ok().body(this.productService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto por id", description = "Permite obtener un producto mediante su identificador")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(this.productService.findOne(id));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Lista productos con stock bajo", description = "Lista productos cuyo stock calculado es menor o igual al umbral configurado")
    public ResponseEntity<List<ProductResponseDTO>> getLowStockProducts() {
        return ResponseEntity.ok(this.productService.findLowStock());
    }

    @PostMapping
    @Operation(summary = "Crea un producto", description = "Permite crear un nuevo producto")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.productService.saveProduct(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un producto", description = "Permite actualizar un producto existente")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequestDTO dto) {
        return ResponseEntity.ok(this.productService.updateProduct(dto, id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un producto", description = "Permite eliminar un producto existente")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        this.productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
