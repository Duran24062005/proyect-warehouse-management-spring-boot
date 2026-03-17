package com.proyectS1.warehouse_management.audit;

import java.util.Map;

import com.proyectS1.warehouse_management.model.enums.OperationType;

public record EntityAuditEvent(
    String entityName,
    String description,
    OperationType operationType,
    Long fallbackActorUserId,
    Map<String, Object> oldValues,
    Map<String, Object> newValues
) {
}
