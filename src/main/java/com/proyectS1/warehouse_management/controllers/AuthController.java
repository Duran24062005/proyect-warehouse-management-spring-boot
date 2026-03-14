package com.proyectS1.warehouse_management.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyectS1.warehouse_management.dtos.request.AuthLoginRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AuthRegisterRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.ChangePasswordRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.AuthLoginResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MessageResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.services.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticacion y perfil")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request")
        }
    )
    @Operation(summary = "Registra un nuevo usuario")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody AuthRegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @PostMapping("/login")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Login completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        }
    )
    @Operation(summary = "Inicia sesion", description = "Valida email y password y retorna un Bearer token")
    public ResponseEntity<AuthLoginResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Authenticated user successfully obtained"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        }
    )
    @Operation(summary = "Obtiene el usuario actual", description = "Retorna el usuario autenticado a partir del token enviado")
    public ResponseEntity<UserResponseDTO> me(Authentication authentication) {
        return ResponseEntity.ok(authService.me(authentication.getName()));
    }

    @PatchMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid data or malformed request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
        }
    )
    @Operation(summary = "Cambia la contrasena del usuario autenticado")
    public ResponseEntity<MessageResponseDTO> changePassword(
        Authentication authentication,
        @Valid @RequestBody ChangePasswordRequestDTO dto
    ) {
        return ResponseEntity.ok(authService.changePassword(authentication.getName(), dto));
    }
}
