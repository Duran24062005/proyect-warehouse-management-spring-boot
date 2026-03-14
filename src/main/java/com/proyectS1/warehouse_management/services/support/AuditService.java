package com.proyectS1.warehouse_management.services.support;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.AuditChange;
import com.proyectS1.warehouse_management.model.TrackedEntity;
import com.proyectS1.warehouse_management.model.enums.OperationType;
import com.proyectS1.warehouse_management.repositories.AuditChangeRepository;
import com.proyectS1.warehouse_management.repositories.TrackedEntityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditService {

    private final AuditChangeRepository auditChangeRepository;
    private final TrackedEntityRepository trackedEntityRepository;
    private final ObjectMapper objectMapper;

    public void logInsert(String entityName, String description, AppUser actorUser, Object newValues) {
        saveAudit(entityName, description, OperationType.INSERT, actorUser, null, newValues);
    }

    public void logUpdate(String entityName, String description, AppUser actorUser, Object oldValues, Object newValues) {
        saveAudit(entityName, description, OperationType.UPDATE, actorUser, oldValues, newValues);
    }

    public void logDelete(String entityName, String description, AppUser actorUser, Object oldValues) {
        saveAudit(entityName, description, OperationType.DELETE, actorUser, oldValues, null);
    }

    private void saveAudit(
        String entityName,
        String description,
        OperationType operationType,
        AppUser actorUser,
        Object oldValues,
        Object newValues
    ) {
        AuditChange auditChange = new AuditChange();
        auditChange.setOperationType(operationType);
        auditChange.setActorUser(actorUser);
        auditChange.setAffectedEntity(resolveTrackedEntity(entityName, description));
        auditChange.setOldValues(toJson(oldValues));
        auditChange.setNewValues(toJson(newValues));
        auditChangeRepository.save(auditChange);
    }

    private TrackedEntity resolveTrackedEntity(String entityName, String description) {
        return trackedEntityRepository.findByName(entityName)
            .orElseGet(() -> trackedEntityRepository.save(new TrackedEntity(entityName, description)));
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
}
