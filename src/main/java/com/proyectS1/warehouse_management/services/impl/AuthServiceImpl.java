package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.AuthLoginRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AuthRegisterRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.ChangePasswordRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.AuthLoginResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.MessageResponseDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.mapper.UserMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO register(AuthRegisterRequestDTO dto) {
        if (appUserRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        AppUser user = userMapper.registerDtoToEntity(dto);
        user.setHashPassword(hashPassword(dto.password()));
        return userMapper.entityToDTO(appUserRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthLoginResponseDTO login(AuthLoginRequestDTO dto) {
        AppUser user = appUserRepository.findByEmail(dto.email())
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.getEnabled()) {
            throw new ResponseStatusException(BAD_REQUEST, "User is disabled");
        }

        if (!user.getHashPassword().equals(hashPassword(dto.password()))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        return new AuthLoginResponseDTO("Login successful. Security token not implemented yet.", userMapper.entityToDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO me(Long userId) {
        return userMapper.entityToDTO(findUserById(userId));
    }

    @Override
    public MessageResponseDTO changePassword(ChangePasswordRequestDTO dto) {
        AppUser user = findUserById(dto.userId());

        if (!user.getHashPassword().equals(hashPassword(dto.currentPassword()))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Current password is invalid");
        }

        if (dto.currentPassword().equals(dto.newPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be different");
        }

        user.setHashPassword(hashPassword(dto.newPassword()));
        appUserRepository.save(user);
        return new MessageResponseDTO("Password changed successfully");
    }

    private AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + userId));
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
