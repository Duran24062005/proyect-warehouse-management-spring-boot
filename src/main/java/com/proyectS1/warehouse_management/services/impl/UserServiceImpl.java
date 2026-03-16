package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.AdminUserRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.AdminUserUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.request.UserStatusUpdateRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.UserResponseDTO;
import com.proyectS1.warehouse_management.mapper.UserMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.model.enums.UserStatus;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.UserService;
import com.proyectS1.warehouse_management.services.support.AuditService;
import com.proyectS1.warehouse_management.services.support.ProfilePhotoStorageService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final AppUserRepository appUserRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserMapper userMapper;
    private final WarehouseAccessService warehouseAccessService;
    private final AuditService auditService;
    private final ProfilePhotoStorageService profilePhotoStorageService;

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
    public List<UserResponseDTO> findByStatus(UserStatus status) {
        return appUserRepository.findByUserStatus(status).stream()
            .map(userMapper::entityToDTO)
            .toList();
    }

    @Override
    public UserResponseDTO createUser(AdminUserRequestDTO dto) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireAdmin(actorUser);
        if (appUserRepository.existsByEmail(dto.email())) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        AppUser user = userMapper.adminDtoToEntity(dto);
        applyRoleAndWarehouse(user, dto.role(), dto.warehouseId());
        user.setHashPassword(hashPassword(dto.password()));
        AppUser savedUser = appUserRepository.save(user);
        UserResponseDTO response = userMapper.entityToDTO(savedUser);
        auditService.logInsert("app_user", "Catalog for application users", actorUser, response);
        return response;
    }

    @Override
    public UserResponseDTO updateUser(Long userId, AdminUserUpdateRequestDTO dto) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireAdmin(actorUser);
        AppUser user = findUserById(userId);
        UserResponseDTO oldValues = userMapper.entityToDTO(user);

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setPhoneNumber(dto.phoneNumber());
        applyRoleAndWarehouse(user, dto.role(), dto.warehouseId());

        AppUser savedUser = appUserRepository.save(user);
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", actorUser, oldValues, newValues);
        return newValues;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> findEmployeesForManagedWarehouses() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        if (currentUser.getRole() != UserRole.USER) {
            throw new ResponseStatusException(FORBIDDEN, "Only managers can consult employees from their warehouses");
        }

        Set<Long> managedWarehouseIds = warehouseAccessService.getManagedWarehouseIds(currentUser);
        if (managedWarehouseIds.isEmpty()) {
            return List.of();
        }

        return appUserRepository.findByRoleAndWarehouseIdIn(UserRole.EMPLOYEE, managedWarehouseIds).stream()
            .map(userMapper::entityToDTO)
            .toList();
    }

    @Override
    public UserResponseDTO updateUserStatus(Long userId, UserStatusUpdateRequestDTO dto) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        AppUser user = findUserById(userId);
        UserResponseDTO oldValues = userMapper.entityToDTO(user);
        AppUser savedUser = saveStatus(user, dto.status());
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", actorUser, oldValues, newValues);
        return newValues;
    }

    @Override
    public UserResponseDTO approveUser(Long userId) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        AppUser user = findUserById(userId);
        if (user.getUserStatus() == UserStatus.ACTIVE) {
            throw new ResponseStatusException(BAD_REQUEST, "User is already active");
        }
        UserResponseDTO oldValues = userMapper.entityToDTO(user);
        AppUser savedUser = saveStatus(user, UserStatus.ACTIVE);
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", actorUser, oldValues, newValues);
        return newValues;
    }

    @Override
    public UserResponseDTO blockUser(Long userId) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        AppUser user = findUserById(userId);
        if (user.getUserStatus() == UserStatus.BLOCKED) {
            throw new ResponseStatusException(BAD_REQUEST, "User is already blocked");
        }
        UserResponseDTO oldValues = userMapper.entityToDTO(user);
        AppUser savedUser = saveStatus(user, UserStatus.BLOCKED);
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", actorUser, oldValues, newValues);
        return newValues;
    }

    @Override
    public UserResponseDTO unblockUser(Long userId) {
        AppUser actorUser = warehouseAccessService.getCurrentUser();
        AppUser user = findUserById(userId);
        if (user.getUserStatus() != UserStatus.BLOCKED) {
            throw new ResponseStatusException(BAD_REQUEST, "Only blocked users can be unblocked");
        }
        UserResponseDTO oldValues = userMapper.entityToDTO(user);
        AppUser savedUser = saveStatus(user, UserStatus.ACTIVE);
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", actorUser, oldValues, newValues);
        return newValues;
    }

    @Override
    public UserResponseDTO uploadProfilePhoto(MultipartFile file) {
        AppUser user = warehouseAccessService.getCurrentUser();
        UserResponseDTO oldValues = userMapper.entityToDTO(user);

        user.setProfilePhotoFilename(
            profilePhotoStorageService.storeProfilePhoto(user.getId(), file, user.getProfilePhotoFilename())
        );

        AppUser savedUser = appUserRepository.save(user);
        UserResponseDTO newValues = userMapper.entityToDTO(savedUser);
        auditService.logUpdate("app_user", "Catalog for application users", savedUser, oldValues, newValues);
        return newValues;
    }

    private AppUser findUserById(Long userId) {
        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + userId));
    }

    private void applyRoleAndWarehouse(AppUser user, UserRole role, Long warehouseId) {
        if (role == UserRole.EMPLOYEE) {
            if (warehouseId == null) {
                throw new ResponseStatusException(BAD_REQUEST, "EMPLOYEE users require warehouseId");
            }

            user.setRole(role);
            user.setWarehouse(resolveWarehouse(warehouseId));
            return;
        }

        if (warehouseId != null) {
            throw new ResponseStatusException(BAD_REQUEST, role + " users must not have warehouseId assigned");
        }

        user.setRole(role);
        user.setWarehouse(null);
    }

    private Warehouse resolveWarehouse(Long warehouseId) {
        return warehouseRepository.findById(warehouseId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Warehouse not found with id " + warehouseId));
    }

    private AppUser saveStatus(AppUser user, UserStatus status) {
        user.setUserStatus(status);
        user.setEnabled(status == UserStatus.ACTIVE);
        return appUserRepository.save(user);
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
