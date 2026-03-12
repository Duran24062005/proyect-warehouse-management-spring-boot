package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.proyectS1.warehouse_management.security.JwtTokenService;
import com.proyectS1.warehouse_management.services.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Override
    public UserResponseDTO register(AuthRegisterRequestDTO dto) {
        if (appUserRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        AppUser user = userMapper.registerDtoToEntity(dto);
        user.setHashPassword(passwordEncoder.encode(dto.password()));
        return userMapper.entityToDTO(appUserRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthLoginResponseDTO login(AuthLoginRequestDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );

        AppUser user = appUserRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid credentials"));

        if (!user.getEnabled()) {
            throw new ResponseStatusException(BAD_REQUEST, "User is disabled");
        }

        String token = jwtTokenService.generateToken(user);
        return new AuthLoginResponseDTO("Login successful", "Bearer", token, userMapper.entityToDTO(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO me(String email) {
        return userMapper.entityToDTO(findUserByEmail(email));
    }

    @Override
    public MessageResponseDTO changePassword(String email, ChangePasswordRequestDTO dto) {
        AppUser user = findUserByEmail(email);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getHashPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Current password is invalid");
        }

        if (dto.currentPassword().equals(dto.newPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "New password must be different");
        }

        user.setHashPassword(passwordEncoder.encode(dto.newPassword()));
        appUserRepository.save(user);
        return new MessageResponseDTO("Password changed successfully");
    }

    private AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + userId));
    }

    private AppUser findUserByEmail(String email) {
        return appUserRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with email " + email));
    }
}
