package com.proyectS1.warehouse_management.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.mapper.UserMapper;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserResponseDTO> findAll() {
        return appUserRepository.findAll().stream()
            .map(userMapper::entityToDTO)
            .toList();
    }

    @Override
    public List<UserResponseDTO> findByRole(UserRole role) {
        return appUserRepository.findByRole(role).stream()
            .map(userMapper::entityToDTO)
            .toList();
    }
}
