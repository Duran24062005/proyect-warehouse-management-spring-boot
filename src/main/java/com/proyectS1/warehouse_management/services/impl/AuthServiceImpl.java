package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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
import com.proyectS1.warehouse_management.model.enums.UserStatus;
import com.proyectS1.warehouse_management.notifications.service.AuthEmailNotificationService;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.security.JwtService;
import com.proyectS1.warehouse_management.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final AuthEmailNotificationService authEmailNotificationService;

    @Override
    public UserResponseDTO register(AuthRegisterRequestDTO dto) {
        if (appUserRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        AppUser user = userMapper.registerDtoToEntity(dto);
        user.setHashPassword(hashPassword(dto.password()));
        AppUser savedUser = appUserRepository.save(user);
        authEmailNotificationService.sendRegistrationEmail(savedUser);
        return userMapper.entityToDTO(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthLoginResponseDTO login(AuthLoginRequestDTO dto) {
        AppUser user = appUserRepository.findByEmail(dto.email())
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (user.getUserStatus() == UserStatus.PENDING) {
            throw new ResponseStatusException(FORBIDDEN, "Account pending approval");
        }

        if (user.getUserStatus() == UserStatus.BLOCKED || !Boolean.TRUE.equals(user.getEnabled())) {
            throw new ResponseStatusException(FORBIDDEN, "Account is blocked");
        }

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(FORBIDDEN, "Account is not active");
        }

        if (!user.getHashPassword().equals(hashPassword(dto.password()))) {
            throw new ResponseStatusException(UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        authEmailNotificationService.sendLoginEmail(user);
        return new AuthLoginResponseDTO("Login successful", token, "Bearer", userMapper.entityToDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO me(String email) {
        return userMapper.entityToDTO(findUserByEmail(email));
    }

    @Override
    public MessageResponseDTO changePassword(String email, ChangePasswordRequestDTO dto) {
        AppUser user = findUserByEmail(email);

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

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with email " + email));
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
