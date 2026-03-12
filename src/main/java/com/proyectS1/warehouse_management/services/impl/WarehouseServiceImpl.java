package com.proyectS1.warehouse_management.services.impl;

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
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.WarehouseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AppUserRepository appUserRepository;
    private final WarehouseMapper warehouseMapper;

    @Override
    public WarehouseResponseDTO saveWarehouse(WarehouseRequestDTO dto) {
        Warehouse warehouse = warehouseMapper.dtoToEntity(dto);
        warehouse.setManager(resolveUser(dto.managerUserId()));
        return warehouseMapper.entityToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponseDTO updateWarehouse(WarehouseRequestDTO dto, Long id) {
        Warehouse warehouse = findWarehouseById(id);
        warehouseMapper.updateEntityFromDTO(warehouse, dto);
        warehouse.setManager(resolveUser(dto.managerUserId()));
        return warehouseMapper.entityToDTO(warehouseRepository.save(warehouse));
    }

    @Override
    public void deleteWarehouse(Long id) {
        warehouseRepository.delete(findWarehouseById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponseDTO> findAll() {
        return warehouseRepository.findAll().stream()
            .map(warehouseMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponseDTO findOne(Long id) {
        return warehouseMapper.entityToDTO(findWarehouseById(id));
    }

    private Warehouse findWarehouseById(Long id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Warehouse not found with id " + id));
    }

    private AppUser resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }

        return appUserRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + userId));
    }
}
