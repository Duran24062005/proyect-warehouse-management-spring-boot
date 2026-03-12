package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.model.enums.UserRole;

public interface UserService {
    List<UserResponseDTO> findAll();
    List<UserResponseDTO> findByRole(UserRole role);
}
