package com.proyectS1.warehouse_management.services;

import java.util.List;

import com.proyectS1.warehouse_management.dtos.request.AdminUserRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AdminUserUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.UserStatusUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;

public interface UserService {
    List<UserResponseDTO> findAll();
    List<UserResponseDTO> findByRole(UserRole role);
    List<UserResponseDTO> findByStatus(UserStatus status);
    UserResponseDTO createUser(AdminUserRequestDTO dto);
    UserResponseDTO updateUser(Long userId, AdminUserUpdateRequestDTO dto);
    List<UserResponseDTO> findEmployeesForManagedWarehouses();
    UserResponseDTO updateUserStatus(Long userId, UserStatusUpdateRequestDTO dto);
    UserResponseDTO approveUser(Long userId);
    UserResponseDTO blockUser(Long userId);
    UserResponseDTO unblockUser(Long userId);
}
