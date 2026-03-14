package com.proyectS1.warehouse_management.mapper;

import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.dtos.request.AdminUserRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AuthRegisterRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;

@Component
public class UserMapper {

    public UserResponseDTO entityToDTO(AppUser user) {
        if (user == null) {
            return null;
        }

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getEnabled(),
            user.getUserStatus()
        );
    }

    public AppUser registerDtoToEntity(AuthRegisterRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        AppUser user = new AppUser();
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());
        user.setRole(UserRole.USER);
        user.setEnabled(Boolean.FALSE);
        user.setUserStatus(UserStatus.PENDING);
        return user;
    }

    public AppUser adminDtoToEntity(AdminUserRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        AppUser user = new AppUser();
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());
        user.setRole(dto.role());
        boolean enabled = dto.enabled() != null ? dto.enabled() : Boolean.TRUE;
        user.setEnabled(enabled);
        user.setUserStatus(enabled ? UserStatus.ACTIVE : UserStatus.BLOCKED);
        return user;
    }
}
