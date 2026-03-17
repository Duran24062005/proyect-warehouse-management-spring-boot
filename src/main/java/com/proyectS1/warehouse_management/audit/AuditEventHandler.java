package com.proyectS1.warehouse_management.audit;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditEventHandler {

    private final AuditService auditService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(EntityAuditEvent event) {
        auditService.persistEvent(event);
    }
}
