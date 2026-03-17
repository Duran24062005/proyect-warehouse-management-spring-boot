package com.proyectS1.warehouse_management.audit;

import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.model.BaseTimeEntity;
import com.proyectS1.warehouse_management.model.enums.OperationType;

import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

public class AuditEntityListener {

    @PostLoad
    public void postLoad(Object entity) {
        if (!(entity instanceof AuditableEntity auditableEntity) || !(entity instanceof BaseTimeEntity baseEntity)) {
            return;
        }

        AuditService auditService = SpringContext.getBean(AuditService.class);
        if (auditService == null) {
            return;
        }

        baseEntity.setAuditSnapshot(auditService.captureSnapshot(auditableEntity, null));
    }

    @PostPersist
    public void postPersist(Object entity) {
        publishEvent(entity, OperationType.INSERT);
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        publishEvent(entity, OperationType.UPDATE);
    }

    @PostRemove
    public void postRemove(Object entity) {
        publishEvent(entity, OperationType.DELETE);
    }

    private void publishEvent(Object entity, OperationType operationType) {
        if (!(entity instanceof AuditableEntity auditableEntity) || !(entity instanceof BaseTimeEntity baseEntity)) {
            return;
        }

        AuditService auditService = SpringContext.getBean(AuditService.class);
        AuditEventPublisher eventPublisher = SpringContext.getBean(AuditEventPublisher.class);
        if (auditService == null || eventPublisher == null) {
            return;
        }

        AuditSnapshot previousSnapshot = baseEntity.getAuditSnapshot();
        AuditSnapshot currentSnapshot = operationType == OperationType.DELETE
            ? null
            : auditService.captureSnapshot(auditableEntity, previousSnapshot);

        EntityAuditEvent event = switch (operationType) {
            case INSERT -> new EntityAuditEvent(
                auditableEntity.auditEntityName(),
                auditableEntity.auditEntityDescription(),
                operationType,
                resolveFallbackActorUserId(entity),
                null,
                currentSnapshot != null ? currentSnapshot.payload() : null
            );
            case UPDATE -> new EntityAuditEvent(
                auditableEntity.auditEntityName(),
                auditableEntity.auditEntityDescription(),
                operationType,
                null,
                previousSnapshot != null ? previousSnapshot.payload() : null,
                currentSnapshot != null ? currentSnapshot.payload() : null
            );
            case DELETE -> new EntityAuditEvent(
                auditableEntity.auditEntityName(),
                auditableEntity.auditEntityDescription(),
                operationType,
                null,
                previousSnapshot != null ? previousSnapshot.payload() : null,
                null
            );
        };

        eventPublisher.publish(event);
        baseEntity.setAuditSnapshot(currentSnapshot);
    }

    private Long resolveFallbackActorUserId(Object entity) {
        if (entity instanceof AppUser user) {
            return user.getId();
        }

        return null;
    }
}
