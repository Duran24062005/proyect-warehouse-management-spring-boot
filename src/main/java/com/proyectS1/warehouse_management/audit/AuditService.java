package com.proyectS1.warehouse_management.audit;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.AuditChange;
import com.proyectS1.warehouse_management.model.Movement;
import com.proyectS1.warehouse_management.model.Product;
import com.proyectS1.warehouse_management.model.TrackedEntity;
import com.proyectS1.warehouse_management.model.Warehouse;
import com.proyectS1.warehouse_management.model.enums.OperationType;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;
import com.proyectS1.warehouse_management.repositories.AuditChangeRepository;
import com.proyectS1.warehouse_management.repositories.TrackedEntityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditChangeRepository auditChangeRepository;
    private final TrackedEntityRepository trackedEntityRepository;
    private final AppUserRepository appUserRepository;
    private final ObjectMapper objectMapper;

    public AuditSnapshot captureSnapshot(AuditableEntity entity, AuditSnapshot previousSnapshot) {
        return switch (entity) {
            case AppUser appUser -> captureAppUserSnapshot(appUser, previousSnapshot);
            case Product product -> new AuditSnapshot(productPayload(product), null);
            case Warehouse warehouse -> new AuditSnapshot(warehousePayload(warehouse), null);
            case Movement movement -> new AuditSnapshot(movementPayload(movement), null);
            default -> throw new IllegalArgumentException("Unsupported auditable entity: " + entity.getClass().getName());
        };
    }

    public void persistEvent(EntityAuditEvent event) {
        AuditChange auditChange = new AuditChange();
        auditChange.setOperationType(event.operationType());
        auditChange.setActorUser(resolveActorUser(event.fallbackActorUserId()));
        auditChange.setAffectedEntity(resolveTrackedEntity(event.entityName(), event.description()));
        auditChange.setOldValues(toJson(event.oldValues()));
        auditChange.setNewValues(toJson(event.newValues()));
        auditChangeRepository.save(auditChange);
    }

    private AuditSnapshot captureAppUserSnapshot(AppUser user, AuditSnapshot previousSnapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", user.getId());
        payload.put("email", user.getEmail());
        payload.put("firstName", user.getFirstName());
        payload.put("lastName", user.getLastName());
        payload.put("phoneNumber", user.getPhoneNumber());
        payload.put("role", user.getRole());
        payload.put("userStatus", user.getUserStatus());
        payload.put("enabled", user.getEnabled());
        payload.put("warehouseId", user.getWarehouse() != null ? user.getWarehouse().getId() : null);
        payload.put("profilePhotoFilename", user.getProfilePhotoFilename());
        if (previousSnapshot != null && !Objects.equals(previousSnapshot.passwordFingerprint(), user.getHashPassword())) {
            payload.put("passwordChanged", true);
        }
        return new AuditSnapshot(payload, user.getHashPassword());
    }

    private Map<String, Object> productPayload(Product product) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", product.getId());
        payload.put("name", product.getName());
        payload.put("category", product.getCategory());
        payload.put("price", safeBigDecimal(product.getPrice()));
        payload.put("warehouseId", product.getWarehouse() != null ? product.getWarehouse().getId() : null);
        return payload;
    }

    private Map<String, Object> warehousePayload(Warehouse warehouse) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", warehouse.getId());
        payload.put("name", warehouse.getName());
        payload.put("ubication", warehouse.getUbication());
        payload.put("capacity", safeBigDecimal(warehouse.getCapacity()));
        payload.put("managerUserId", warehouse.getManager() != null ? warehouse.getManager().getId() : null);
        return payload;
    }

    private Map<String, Object> movementPayload(Movement movement) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", movement.getId());
        payload.put("movementType", movement.getMovementType());
        payload.put("registeredByUserId", movement.getRegisteredByUser() != null ? movement.getRegisteredByUser().getId() : null);
        payload.put("performedByEmployeeId", movement.getPerformedByEmployee() != null ? movement.getPerformedByEmployee().getId() : null);
        payload.put("originWarehouseId", movement.getOriginWarehouse() != null ? movement.getOriginWarehouse().getId() : null);
        payload.put("destinationWarehouseId", movement.getDestinationWarehouse() != null ? movement.getDestinationWarehouse().getId() : null);
        payload.put("productId", movement.getProduct() != null ? movement.getProduct().getId() : null);
        return payload;
    }

    private TrackedEntity resolveTrackedEntity(String entityName, String description) {
        return trackedEntityRepository.findByName(entityName)
            .orElseGet(() -> trackedEntityRepository.save(new TrackedEntity(entityName, description)));
    }

    private AppUser resolveActorUser(Long fallbackActorUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null && !"anonymousUser".equals(authentication.getName())) {
            return appUserRepository.findByEmail(authentication.getName()).orElse(null);
        }

        if (fallbackActorUserId == null) {
            return null;
        }

        return appUserRepository.findById(fallbackActorUserId).orElse(null);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize audit payload", exception);
        }
    }

    private Object safeBigDecimal(BigDecimal value) {
        return value != null ? value.toPlainString() : null;
    }
}
