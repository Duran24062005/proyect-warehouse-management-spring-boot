package com.proyectS1.warehouse_management.audit;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(EntityAuditEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
