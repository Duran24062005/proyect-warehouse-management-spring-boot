package com.proyectS1.warehouse_management.services;

import com.proyectS1.warehouse_management.dtos.request.AuthLoginRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AuthRegisterRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.ChangePasswordRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.AuthLoginResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MessageResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;

public interface AuthService {
    UserResponseDTO register(AuthRegisterRequestDTO dto);
    AuthLoginResponseDTO login(AuthLoginRequestDTO dto);
    UserResponseDTO me(Long userId);
    MessageResponseDTO changePassword(ChangePasswordRequestDTO dto);
}
