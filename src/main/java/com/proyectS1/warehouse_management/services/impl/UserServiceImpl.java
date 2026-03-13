package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.CONFLICT;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.AdminUserRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.mapper.UserMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.services.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
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

    @Override
    public UserResponseDTO createUser(AdminUserRequestDTO dto) {
        if (appUserRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        AppUser user = userMapper.adminDtoToEntity(dto);
        user.setHashPassword(hashPassword(dto.password()));
        return userMapper.entityToDTO(appUserRepository.save(user));
    }

    private String hashPassword(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
