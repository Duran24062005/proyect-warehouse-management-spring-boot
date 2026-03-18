package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.dtos.request.MovementRequestDTO;
import com.proyectS1.warehouse_management.dtos.response.MovementResponseDTO;
import com.proyectS1.warehouse_management.mapper.MovementMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.Movement;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.MovementType;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.MovementRepository;
import com.proyectS1.warehouse_management.repositories.ProductRepository;
import com.proyectS1.warehouse_management.repositories.WarehouseRepository;
import com.proyectS1.warehouse_management.services.MovementService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MovementServiceImpl implements MovementService {

    private final AppUserRepository appUserRepository;
    private final MovementRepository movementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final MovementMapper movementMapper;
    private final WarehouseAccessService warehouseAccessService;

    @Override
    public MovementResponseDTO saveMovement(MovementRequestDTO dto) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        validateWarehouseRules(dto);
        Product product = resolveProduct(dto.productId());
        warehouseAccessService.requireAnyWarehouseAccess(currentUser, getParticipatingWarehouseIds(dto));
        validateProductLocation(product, dto, getWarehouseId(product.getWarehouse()));

        Movement movement = movementMapper.dtoToEntity(dto);
        hydrateRelations(movement, dto, currentUser, product);
        Movement savedMovement = saveAndFlushMovement(movement);
        product.setWarehouse(resolveCurrentWarehouseAfterMovement(dto));
        productRepository.save(product);
        return movementMapper.entityToDTO(savedMovement);
    }

    @Override
    public MovementResponseDTO updateMovement(MovementRequestDTO dto, Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        validateWarehouseRules(dto);

        Movement movement = findMovementById(id);
        requireMovementAccess(currentUser, movement);
        requireLatestMovementMutation(movement);
        if (!Objects.equals(movement.getProduct().getId(), dto.productId())) {
            throw new ResponseStatusException(BAD_REQUEST, "The product assigned to a movement cannot be changed");
        }

        Product product = movement.getProduct();
        Long previousWarehouseId = getWarehouseIdBeforeMovement(movement);
        warehouseAccessService.requireAnyWarehouseAccess(currentUser, getParticipatingWarehouseIds(dto));
        validateProductLocation(product, dto, previousWarehouseId);
        movementMapper.updateEntityFromDTO(movement, dto);
        hydrateRelations(movement, dto, currentUser, product);
        Movement savedMovement = saveAndFlushMovement(movement);
        product.setWarehouse(resolveCurrentWarehouseAfterMovement(dto));
        productRepository.save(product);
        return movementMapper.entityToDTO(savedMovement);
    }

    @Override
    public void deleteMovement(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Movement movement = findMovementById(id);
        requireMovementAccess(currentUser, movement);
        requireLatestMovementMutation(movement);
        Product product = movement.getProduct();
        product.setWarehouse(resolveWarehouse(getWarehouseIdBeforeMovement(movement)));
        movementRepository.delete(movement);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findAll() {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        List<Movement> movements;
        if (warehouseAccessService.isAdmin(currentUser)) {
            movements = movementRepository.findAll();
        } else {
            Set<Long> managedWarehouseIds = warehouseAccessService.getManagedWarehouseIds(currentUser);
            movements = managedWarehouseIds.isEmpty()
                ? List.of()
                : movementRepository.findByOriginWarehouseIdInOrDestinationWarehouseIdIn(managedWarehouseIds, managedWarehouseIds);
        }
        return movements.stream()
            .map(movementMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovementResponseDTO findOne(Long id) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        Movement movement = findMovementById(id);
        requireMovementAccess(currentUser, movement);
        return movementMapper.entityToDTO(movement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findByProduct(Long productId) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        return movementRepository.findByProductId(productId).stream()
            .filter(movement -> hasMovementAccess(currentUser, movement))
            .map(movementMapper::entityToDTO)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementResponseDTO> findByWarehouse(Long warehouseId) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        return movementRepository.findByOriginWarehouseIdOrDestinationWarehouseId(warehouseId, warehouseId).stream()
            .filter(movement -> hasMovementAccess(currentUser, movement))
            .map(movementMapper::entityToDTO)
            .toList();
    }

    private void hydrateRelations(Movement movement, MovementRequestDTO dto, AppUser currentUser, Product product) {
        movement.setRegisteredByUser(currentUser);
        movement.setPerformedByEmployee(resolvePerformedByEmployee(dto, currentUser));
        movement.setOriginWarehouse(resolveWarehouse(dto.originWarehouseId()));
        movement.setDestinationWarehouse(resolveWarehouse(dto.destinationWarehouseId()));
        movement.setProduct(product);
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

    private void validateProductLocation(Product product, MovementRequestDTO dto, Long currentWarehouseId) {
        switch (dto.movementType()) {
            case ENTRY -> {
                if (currentWarehouseId != null) {
                    throw new ResponseStatusException(
                        BAD_REQUEST,
                        "ENTRY is only valid for assets that are currently outside any warehouse"
                    );
                }
            }
            case EXIT -> {
                if (!Objects.equals(currentWarehouseId, dto.originWarehouseId())) {
                    throw new ResponseStatusException(
                        BAD_REQUEST,
                        "EXIT requires the asset to currently belong to the selected origin warehouse"
                    );
                }
            }
            case TRANSFER -> {
                if (!Objects.equals(currentWarehouseId, dto.originWarehouseId())) {
                    throw new ResponseStatusException(
                        BAD_REQUEST,
                        "TRANSFER requires the asset to currently belong to the selected origin warehouse"
                    );
                }
            }
        }
    }

    private AppUser resolvePerformedByEmployee(MovementRequestDTO dto, AppUser currentUser) {
        AppUser employee = appUserRepository.findById(dto.performedByEmployeeId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Employee not found with id " + dto.performedByEmployeeId()));

        if (employee.getRole() != UserRole.EMPLOYEE) {
            throw new ResponseStatusException(BAD_REQUEST, "performedByEmployeeId must belong to a user with EMPLOYEE role");
        }

        Long employeeWarehouseId = employee.getWarehouse() != null ? employee.getWarehouse().getId() : null;
        if (employeeWarehouseId == null) {
            throw new ResponseStatusException(BAD_REQUEST, "Selected employee does not have an assigned warehouse");
        }

        boolean matchesMovementWarehouse = switch (dto.movementType()) {
            case ENTRY -> employeeWarehouseId.equals(dto.destinationWarehouseId());
            case EXIT -> employeeWarehouseId.equals(dto.originWarehouseId());
            case TRANSFER -> employeeWarehouseId.equals(dto.originWarehouseId())
                || employeeWarehouseId.equals(dto.destinationWarehouseId());
        };

        if (!matchesMovementWarehouse) {
            throw new ResponseStatusException(BAD_REQUEST, "Selected employee must belong to a warehouse participating in the movement");
        }

        if (!warehouseAccessService.isAdmin(currentUser)) {
            warehouseAccessService.requireWarehouseAccess(currentUser, employeeWarehouseId);
        }

        return employee;
    }

    private Movement saveAndFlushMovement(Movement movement) {
        try {
            return movementRepository.saveAndFlush(movement);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(BAD_REQUEST, "Movement data violates a database rule", exception);
        }
    }

    private List<Long> getParticipatingWarehouseIds(MovementRequestDTO dto) {
        return java.util.stream.Stream.of(dto.originWarehouseId(), dto.destinationWarehouseId())
            .filter(Objects::nonNull)
            .toList();
    }

    private void requireLatestMovementMutation(Movement movement) {
        Movement latestMovement = movementRepository.findTopByProductIdOrderByCreatedAtDescIdDesc(movement.getProduct().getId())
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No movements found for product " + movement.getProduct().getId()));

        if (!Objects.equals(latestMovement.getId(), movement.getId())) {
            throw new ResponseStatusException(
                BAD_REQUEST,
                "Only the latest movement for an asset can be edited or deleted"
            );
        }
    }

    private Long getWarehouseIdBeforeMovement(Movement movement) {
        return switch (movement.getMovementType()) {
            case ENTRY -> null;
            case EXIT, TRANSFER -> getWarehouseId(movement.getOriginWarehouse());
        };
    }

    private Warehouse resolveCurrentWarehouseAfterMovement(MovementRequestDTO dto) {
        return switch (dto.movementType()) {
            case ENTRY, TRANSFER -> resolveWarehouse(dto.destinationWarehouseId());
            case EXIT -> null;
        };
    }

    private Long getWarehouseId(Warehouse warehouse) {
        return warehouse != null ? warehouse.getId() : null;
    }

    private void requireMovementAccess(AppUser currentUser, Movement movement) {
        if (!hasMovementAccess(currentUser, movement)) {
            throw new ResponseStatusException(FORBIDDEN, "Access denied for movement " + movement.getId());
        }
    }

    private boolean hasMovementAccess(AppUser currentUser, Movement movement) {
        if (warehouseAccessService.isAdmin(currentUser)) {
            return true;
        }

        Set<Long> managedWarehouseIds = warehouseAccessService.getManagedWarehouseIds(currentUser);
        Long originWarehouseId = movement.getOriginWarehouse() != null ? movement.getOriginWarehouse().getId() : null;
        Long destinationWarehouseId = movement.getDestinationWarehouse() != null ? movement.getDestinationWarehouse().getId() : null;

        return (originWarehouseId != null && managedWarehouseIds.contains(originWarehouseId))
            || (destinationWarehouseId != null && managedWarehouseIds.contains(destinationWarehouseId));
    }
}
