package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.MovementRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;
import com.proyectS1.warehouse_management.mapper.MovementMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Movement;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.MovementType;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.MovementRepository;
import com.proyectS1.warehouse_management.repositories.ProductRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.MovementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;
    private final AppUserRepository appUserRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final MovementMapper movementMapper;

    @Override
    public MovementResponseDTO saveMovement(MovementRequestDTO dto) {
        validateWarehouseRules(dto);

        Movement movement = movementMapper.dtoToEntity(dto);
        hydrateRelations(movement, dto);
        return movementMapper.entityToDTO(movementRepository.save(movement));
    }

    @Override
    public MovementResponseDTO updateMovement(MovementRequestDTO dto, Long id) {
        validateWarehouseRules(dto);

        Movement movement = findMovementById(id);
        movementMapper.updateEntityFromDTO(movement, dto);
        hydrateRelations(movement, dto);
        return movementMapper.entityToDTO(movementRepository.save(movement));
    }

    @Override
    public void deleteMovement(Long id) {
        movementRepository.delete(findMovementById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findAll() {
        return movementRepository.findAll().stream()
            .map(movementMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovementResponseDTO findOne(Long id) {
        return movementMapper.entityToDTO(findMovementById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findByProduct(Long productId) {
        return movementRepository.findByProductId(productId).stream()
            .map(movementMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findByWarehouse(Long warehouseId) {
        return movementRepository.findByOriginWarehouseIdOrDestinationWarehouseId(warehouseId, warehouseId).stream()
            .map(movementMapper::entityToDTO)
            .toList();
    }

    private void hydrateRelations(Movement movement, MovementRequestDTO dto) {
        movement.setEmployee(resolveUser(dto.employeeUserId()));
        movement.setOriginWarehouse(resolveWarehouse(dto.originWarehouseId()));
        movement.setDestinationWarehouse(resolveWarehouse(dto.destinationWarehouseId()));
        movement.setProduct(resolveProduct(dto.productId()));
    }

    private void validateWarehouseRules(MovementRequestDTO dto) {
        MovementType type = dto.movementType();
        Long originWarehouseId = dto.originWarehouseId();
        Long destinationWarehouseId = dto.destinationWarehouseId();

        if (type == MovementType.ENTRY && (originWarehouseId != null || destinationWarehouseId == null)) {
            throw new ResponseStatusException(BAD_REQUEST, "ENTRY requires destinationWarehouseId and no originWarehouseId");
        }

        if (type == MovementType.EXIT && (originWarehouseId == null || destinationWarehouseId != null)) {
            throw new ResponseStatusException(BAD_REQUEST, "EXIT requires originWarehouseId and no destinationWarehouseId");
        }

        if (type == MovementType.TRANSFER && (originWarehouseId == null || destinationWarehouseId == null)) {
            throw new ResponseStatusException(BAD_REQUEST, "TRANSFER requires originWarehouseId and destinationWarehouseId");
        }

        if (type == MovementType.TRANSFER && originWarehouseId.equals(destinationWarehouseId)) {
            throw new ResponseStatusException(BAD_REQUEST, "TRANSFER origin and destination warehouses must be different");
        }
    }

    private Movement findMovementById(Long id) {
        return movementRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Movement not found with id " + id));
    }

    private AppUser resolveUser(Long id) {
        return appUserRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found with id " + id));
    }

    private Warehouse resolveWarehouse(Long id) {
        if (id == null) {
            return null;
        }

        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Warehouse not found with id " + id));
    }

    private Product resolveProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found with id " + id));
    }
}
