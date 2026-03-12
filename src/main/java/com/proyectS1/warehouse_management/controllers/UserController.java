package com.proyectS1.warehouse_management.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints de usuarios para pruebas administrativas sin seguridad aplicada")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/role")
    @Operation(summary = "Lista usuarios por rol")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@RequestParam UserRole role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }
}
