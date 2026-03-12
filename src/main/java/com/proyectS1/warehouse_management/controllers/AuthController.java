package com.proyectS1.warehouse_management.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.request.AuthLoginRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AuthRegisterRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.ChangePasswordRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.AuthLoginResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MessageResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticacion sin seguridad aplicada todavia")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registra un nuevo usuario")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody AuthRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de prueba", description = "Valida email y password pero no genera token todavia")
    public ResponseEntity<AuthLoginResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtiene el usuario actual", description = "Mientras no exista seguridad, se consulta usando userId como request param")
    public ResponseEntity<UserResponseDTO> me(@RequestParam Long userId) {
        return ResponseEntity.ok(authService.me(userId));
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Cambia la contrasena del usuario")
    public ResponseEntity<MessageResponseDTO> changePassword(@Valid @RequestBody ChangePasswordRequestDTO dto) {
        return ResponseEntity.ok(authService.changePassword(dto));
    }
}
