package com.proyectS1.warehouse_management.services.impl;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.dtos.response.AuditChangeResponseDTO;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.AuditChange;
import com.proyectS1.warehouse_management.model.enums.OperationType;
import com.proyectS1.warehouse_management.model.enums.UserRole;
import com.proyectS1.warehouse_management.repositories.AuditChangeRepository;
import com.proyectS1.warehouse_management.services.AuditChangeService;
import com.proyectS1.warehouse_management.services.support.WarehouseAccessService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditChangeServiceImpl implements AuditChangeService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuditChangeRepository auditChangeRepository;
    private final WarehouseAccessService warehouseAccessService;
    private final ObjectMapper objectMapper;

    @Override
    public List<AuditChangeResponseDTO> findVisibleAuditChanges(
        OperationType operationType,
        String entityName,
        Long actorUserId,
        Long warehouseId
    ) {
        AppUser currentUser = warehouseAccessService.getCurrentUser();
        if (currentUser.getRole() == UserRole.EMPLOYEE) {
            throw new ResponseStatusException(FORBIDDEN, "Employees cannot access audit history");
        }

        Set<Long> visibleWarehouseIds = warehouseAccessService.isAdmin(currentUser)
            ? Set.of()
            : warehouseAccessService.getManagedWarehouseIds(currentUser);

        if (!warehouseAccessService.isAdmin(currentUser) && visibleWarehouseIds.isEmpty()) {
            return List.of();
        }

        final String normalizedEntityName = normalizeEntityName(entityName);

        return auditChangeRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toProjection)
            .filter(item -> matchesOperationType(item, operationType))
            .filter(item -> matchesEntityName(item, normalizedEntityName))
            .filter(item -> matchesActor(item, actorUserId))
            .filter(item -> matchesRoleScope(item, currentUser, visibleWarehouseIds))
            .filter(item -> matchesWarehouseFilter(item, warehouseId))
            .map(this::toResponse)
            .toList();
    }

    private AuditProjection toProjection(AuditChange auditChange) {
        Map<String, Object> oldValues = parsePayload(auditChange.getOldValues());
        Map<String, Object> newValues = parsePayload(auditChange.getNewValues());
        return new AuditProjection(
            auditChange,
            oldValues,
            newValues,
            extractWarehouseIds(
                auditChange.getAffectedEntity() != null ? auditChange.getAffectedEntity().getName() : null,
                oldValues,
                newValues
            )
        );
    }

    private Map<String, Object> parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readValue(payload, MAP_TYPE);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to parse audit payload", exception);
        }
    }

    private boolean matchesOperationType(AuditProjection projection, OperationType operationType) {
        return operationType == null || projection.auditChange().getOperationType() == operationType;
    }

    private boolean matchesEntityName(AuditProjection projection, String entityName) {
        if (entityName == null) {
            return true;
        }

        String currentEntityName = projection.auditChange().getAffectedEntity() != null
            ? projection.auditChange().getAffectedEntity().getName()
            : null;
        return entityName.equalsIgnoreCase(currentEntityName);
    }

    private boolean matchesActor(AuditProjection projection, Long actorUserId) {
        if (actorUserId == null) {
            return true;
        }

        AppUser actorUser = projection.auditChange().getActorUser();
        return actorUser != null && actorUserId.equals(actorUser.getId());
    }

    private boolean matchesRoleScope(AuditProjection projection, AppUser currentUser, Set<Long> visibleWarehouseIds) {
        if (warehouseAccessService.isAdmin(currentUser)) {
            return true;
        }

        if (projection.warehouseIds().isEmpty()) {
            return false;
        }

        return projection.warehouseIds().stream().anyMatch(visibleWarehouseIds::contains);
    }

    private boolean matchesWarehouseFilter(AuditProjection projection, Long warehouseId) {
        return warehouseId == null || projection.warehouseIds().contains(warehouseId);
    }

    private Set<Long> extractWarehouseIds(String entityName, Map<String, Object> oldValues, Map<String, Object> newValues) {
        Set<Long> warehouseIds = new LinkedHashSet<>();
        String normalizedEntityName = normalizeEntityName(entityName);

        if (normalizedEntityName == null) {
            return warehouseIds;
        }

        switch (normalizedEntityName) {
            case "product" -> {
                addWarehouseId(warehouseIds, oldValues, "warehouseId");
                addWarehouseId(warehouseIds, newValues, "warehouseId");
            }
            case "movement" -> {
                addWarehouseId(warehouseIds, oldValues, "originWarehouseId");
                addWarehouseId(warehouseIds, oldValues, "destinationWarehouseId");
                addWarehouseId(warehouseIds, newValues, "originWarehouseId");
                addWarehouseId(warehouseIds, newValues, "destinationWarehouseId");
            }
            case "warehouse" -> {
                addWarehouseId(warehouseIds, oldValues, "id");
                addWarehouseId(warehouseIds, newValues, "id");
            }
            case "app_user" -> {
                addWarehouseId(warehouseIds, oldValues, "warehouseId");
                addWarehouseId(warehouseIds, newValues, "warehouseId");
            }
            default -> {
            }
        }

        return warehouseIds;
    }

    private void addWarehouseId(Collection<Long> warehouseIds, Map<String, Object> values, String field) {
        Long warehouseId = readLong(values, field);
        if (warehouseId != null) {
            warehouseIds.add(warehouseId);
        }
    }

    private Long readLong(Map<String, Object> values, String field) {
        if (values == null || !values.containsKey(field)) {
            return null;
        }

        Object rawValue = values.get(field);
        if (rawValue == null) {
            return null;
        }

        if (rawValue instanceof Number number) {
            return number.longValue();
        }

        if (rawValue instanceof String stringValue && !stringValue.isBlank()) {
            try {
                return Long.parseLong(stringValue);
            } catch (NumberFormatException _exception) {
                return null;
            }
        }

        return null;
    }

    private String normalizeEntityName(String entityName) {
        if (entityName == null || entityName.isBlank()) {
            return null;
        }

        return entityName.trim().toLowerCase();
    }

    private AuditChangeResponseDTO toResponse(AuditProjection projection) {
        AuditChange auditChange = projection.auditChange();
        AppUser actorUser = auditChange.getActorUser();
        String actorUserName = actorUser == null
            ? null
            : ("%s %s".formatted(actorUser.getFirstName(), actorUser.getLastName())).trim();

        return new AuditChangeResponseDTO(
            auditChange.getId(),
            auditChange.getCreatedAt(),
            auditChange.getOperationType(),
            auditChange.getAffectedEntity() != null ? auditChange.getAffectedEntity().getName() : null,
            auditChange.getAffectedEntity() != null ? auditChange.getAffectedEntity().getDescription() : null,
            actorUser != null ? actorUser.getId() : null,
            actorUserName == null || actorUserName.isBlank() ? null : actorUserName,
            actorUser != null ? actorUser.getEmail() : null,
            projection.oldValues() != null ? new LinkedHashMap<>(projection.oldValues()) : null,
            projection.newValues() != null ? new LinkedHashMap<>(projection.newValues()) : null
        );
    }

    private record AuditProjection(
        AuditChange auditChange,
        Map<String, Object> oldValues,
        Map<String, Object> newValues,
        Set<Long> warehouseIds
    ) {
    }
}
