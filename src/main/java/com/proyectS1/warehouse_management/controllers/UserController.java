package com.proyectS1.warehouse_management.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.request.AdminUserRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AdminUserUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.UserStatusUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;
import com.proyectS1.warehouse_management.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Endpoints de usuarios para pruebas administrativas sin seguridad aplicada")
public class UserController {

    private final UserService userService;

    @GetMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Users successfully obtained"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Lista todos los usuarios")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/role")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Users by role successfully obtained"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Lista usuarios por rol")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@RequestParam UserRole role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    @GetMapping("/status")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Users by status successfully obtained"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Lista usuarios por estado")
    public ResponseEntity<List<UserResponseDTO>> getUsersByStatus(@RequestParam UserStatus status) {
        return ResponseEntity.ok(userService.findByStatus(status));
    }

    @GetMapping("/employees/my-warehouses")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Employees for managed warehouses successfully obtained"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Lista empleados de las bodegas gestionadas por el manager autenticado")
    public ResponseEntity<List<UserResponseDTO>> getEmployeesForManagedWarehouses() {
        return ResponseEntity.ok(userService.findEmployeesForManagedWarehouses());
    }

    @PostMapping
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    @Operation(summary = "Crea un usuario desde administracion")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AdminUserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @PutMapping("/{id}")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "User successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User or warehouse not found")
        }
    )
    @Operation(summary = "Actualiza datos administrativos de un usuario")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateRequestDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PatchMapping("/{id}/status")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "User status successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @Operation(summary = "Actualiza el estado de un usuario")
    public ResponseEntity<UserResponseDTO> updateUserStatus(
        @PathVariable Long id,
        @Valid @RequestBody UserStatusUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(id, dto));
    }

    @PatchMapping("/{id}/approve")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "User successfully approved"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @Operation(summary = "Aprueba un usuario pendiente o reactiva un usuario no activo")
    public ResponseEntity<UserResponseDTO> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.approveUser(id));
    }

    @PatchMapping("/{id}/block")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "User successfully blocked"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @Operation(summary = "Bloquea un usuario")
    public ResponseEntity<UserResponseDTO> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PatchMapping("/{id}/unblock")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "User successfully unblocked"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @Operation(summary = "Desbloquea un usuario bloqueado")
    public ResponseEntity<UserResponseDTO> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }
}
