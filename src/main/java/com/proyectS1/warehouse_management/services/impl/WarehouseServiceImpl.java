package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.WarehouseRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.WarehouseResponseDTO;
import com.proyectS1.warehouse_management.mapper.WarehouseMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.WarehouseService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AppUserRepository appUserRepository;
    private final WarehouseMapper warehouseMapper;
    private final WarehouseAccessService warehouseAccessService;

    @Override
    public WarehouseResponseDTO saveWarehouse(WarehouseRequestDTO dto) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireAdmin(currentUser);
        Warehouse warehouse = warehouseMapper.dtoToEntity(dto);
        warehouse.setManager(resolveUser(dto.managerUserId()));
        return warehouseMapper.entityToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO dto, Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireAdmin(currentUser);
        Warehouse warehouse = findWarehouseById(id);
        warehouseMapper.updateEntityFromDTO(warehouse, dto);
        warehouse.setManager(resolveUser(dto.managerUserId()));
        return warehouseMapper.entityToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public void deleteWarehouse(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        warehouseAccessService.requireAdmin(currentUser);
        Warehouse warehouse = findWarehouseById(id);
        warehouseRepository.delete(warehouse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> findAll() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        List<Warehouse> warehouses = warehouseAccessService.isAdmin(currentUser)
            ? warehouseRepository.findAll()
            : warehouseRepository.findByManagerId(currentUser.getId());
        return warehouses.stream()
            .map(warehouseMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> findAllForReferences() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            throw new ResponseStatusException(FORBIDDEN, "Employees cannot access the warehouse reference catalog");
        }

        return warehouseRepository.findAll().stream()
            .map(warehouseMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponseDTO findOne(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Warehouse warehouse = findWarehouseById(id);
        warehouseAccessService.requireWarehouseAccess(currentUser, warehouse.getId());
        return warehouseMapper.entityToDTO(warehouse);
    }

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Warehouse not found with id " + id));
    }

    private AppUser resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }

        AppUser user = appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + userId));

        if (user.getRole() != UserRole.USER) {
            throw new ResponseStatusException(BAD_REQUEST, "Only users with USER role can be assigned as warehouse manager");
        }

        return user;
    }
}
